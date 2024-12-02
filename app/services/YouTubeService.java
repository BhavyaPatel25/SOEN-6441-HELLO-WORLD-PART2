package services;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Service for interacting with the YouTube Data API.
 *
 * <p>This service provides methods for performing various operations, including video searches,
 * fetching channel profiles, retrieving video details, and analyzing video descriptions.</p>
 *
 * <p>Dependencies:
 * <ul>
 *   <li>Google API Client Library for Java</li>
 *   <li>YouTube Data API</li>
 * </ul>
 * </p>
 */
@Singleton
public class YouTubeService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);
    private static final String API_KEY = "AIzaSyCXUSiZS7g6ZFryc1g0FpUivq5nPkWc-rk"; // Replace with your actual API key
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private final YouTube youtubeService;

    /**
     * Initializes the YouTube service with the required API client configurations.
     */
    @Inject
    public YouTubeService() {
        this.youtubeService = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, request -> {})
                .setApplicationName("Reactive TubeLytics")
                .build();
    }

    /**
     * Performs a search query on YouTube and retrieves video details asynchronously.
     *
     * @param query the search term or query string
     * @return a {@code CompletionStage} containing a list of maps, where each map represents a video's details
     */
    public CompletionStage<List<Map<String, String>>> search(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YouTube.Search.List search = youtubeService.search().list("snippet");
                search.setQ(query);
                search.setType("video");
                search.setMaxResults(10L);
                search.setKey(API_KEY);

                SearchListResponse response = search.execute();
                List<SearchResult> results = response.getItems();

                return results.stream().map(item -> {
                    Map<String, String> video = new HashMap<>();
                    video.put("videoId", item.getId().getVideoId());
                    video.put("title", item.getSnippet().getTitle());
                    video.put("description", item.getSnippet().getDescription());
                    video.put("channelTitle", item.getSnippet().getChannelTitle());
                    video.put("thumbnailUrl", item.getSnippet().getThumbnails().getDefault().getUrl());
                    return video;
                }).collect(Collectors.toList());

            } catch (Exception e) {
                logger.error("Error fetching video search results", e);
                return Collections.emptyList();
            }
        });
    }

    /**
     * Fetches video details based on a search query.
     *
     * @param query the search query string
     * @return a {@code CompletableFuture} containing a list of maps with video details
     */
    public CompletableFuture<List<Map<String, String>>> searchVideosAsync(String query) {
        return CompletableFuture.supplyAsync(() -> {
            List<Map<String, String>> results = new ArrayList<>();
            try {
                YouTube.Search.List searchRequest = youtubeService.search().list("snippet");
                searchRequest.setQ(query);
                searchRequest.setType("video");
                searchRequest.setMaxResults(10L);
                searchRequest.setKey(API_KEY);

                SearchListResponse searchResponse = searchRequest.execute();

                List<String> videoIds = searchResponse.getItems().stream()
                        .map(item -> item.getId().getVideoId())
                        .collect(Collectors.toList());

                YouTube.Videos.List videosRequest = youtubeService.videos().list("snippet");
                videosRequest.setId(String.join(",", videoIds));
                videosRequest.setKey(API_KEY);

                VideoListResponse videosResponse = videosRequest.execute();

                for (Video item : videosResponse.getItems()) {
                    Map<String, String> videoDetails = new HashMap<>();
                    videoDetails.put("videoId", item.getId());
                    videoDetails.put("title", item.getSnippet().getTitle());
                    videoDetails.put("description", item.getSnippet().getDescription());
                    videoDetails.put("channelTitle", item.getSnippet().getChannelTitle());
                    videoDetails.put("channelId", item.getSnippet().getChannelId());
                    videoDetails.put("defaultThumbnail", item.getSnippet().getThumbnails().getDefault().getUrl());

                    if (item.getSnippet().getTags() != null) {
                        videoDetails.put("tags", String.join(", ", item.getSnippet().getTags()));
                    } else {
                        videoDetails.put("tags", "No tags available");
                    }

                    results.add(videoDetails);
                }

            } catch (IOException e) {
                logger.error("Error fetching video search results", e);
            }
            return results;
        });
    }

    /**
     * Retrieves details of a video by its ID.
     *
     * @param videoId the unique ID of the video
     * @return a map containing video details such as title, description, likes, views, and tags
     */
    public Map<String, String> getVideoDetails(String videoId) {
        try {
            YouTube.Videos.List request = youtubeService.videos().list("snippet,statistics");
            request.setId(videoId);
            request.setKey(API_KEY);

            VideoListResponse response = request.execute();
            if (!response.getItems().isEmpty()) {
                Video video = response.getItems().get(0);
                Map<String, String> videoDetails = new HashMap<>();
                videoDetails.put("title", video.getSnippet().getTitle());
                videoDetails.put("description", video.getSnippet().getDescription());
                videoDetails.put("likes", String.valueOf(video.getStatistics().getLikeCount()));
                videoDetails.put("views", String.valueOf(video.getStatistics().getViewCount()));
                videoDetails.put("tags", String.join(", ", video.getSnippet().getTags()));
                return videoDetails;
            }
        } catch (IOException e) {
            logger.error("Error fetching video details", e);
        }
        return Collections.emptyMap();
    }

    /**
     * Asynchronously fetches profile information of a YouTube channel.
     *
     * @param channelId the unique ID of the YouTube channel
     * @return a {@code CompletableFuture} containing a map with channel profile details
     */
    public CompletableFuture<Map<String, Object>> fetchChannelProfile(String channelId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YouTube.Channels.List request = youtubeService.channels().list("snippet,statistics,contentDetails");
                request.setId(channelId);
                request.setKey(API_KEY);

                ChannelListResponse response = request.execute();
                if (response.getItems() == null || response.getItems().isEmpty()) {
                    return Collections.emptyMap();
                }

                Channel channel = response.getItems().get(0);
                if (channel == null || channel.getSnippet() == null || channel.getStatistics() == null) {
                    return Collections.emptyMap();
                }

                Map<String, Object> profileData = new HashMap<>();
                profileData.put("title", channel.getSnippet().getTitle());
                profileData.put("description", channel.getSnippet().getDescription());
                profileData.put("thumbnail", channel.getSnippet().getThumbnails().getDefault().getUrl());
                profileData.put("subscriberCount", channel.getStatistics().getSubscriberCount());
                profileData.put("videoCount", channel.getStatistics().getVideoCount());

                if (channel.getContentDetails() != null
                        && channel.getContentDetails().getRelatedPlaylists() != null
                        && channel.getContentDetails().getRelatedPlaylists().getUploads() != null) {
                    String uploadsPlaylistId = channel.getContentDetails().getRelatedPlaylists().getUploads();
                    profileData.put("recentVideos", fetchRecentVideos(uploadsPlaylistId));
                } else {
                    profileData.put("recentVideos", Collections.emptyList());
                }

                return profileData;
            } catch (Exception e) {
                logger.error("Error fetching channel profile", e);
                return Collections.emptyMap();
            }
        });
    }

    /**
     * Fetches descriptions of videos based on a search query asynchronously.
     *
     * @param query the search query string
     * @return a {@code CompletableFuture} containing a list of video descriptions
     */
    public CompletableFuture<List<String>> fetchDescriptionsByQuery(String query) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> descriptions = new ArrayList<>();
            try {
                YouTube.Search.List searchRequest = youtubeService.search().list("snippet");
                searchRequest.setQ(query);
                searchRequest.setType("video");
                searchRequest.setMaxResults(50L);
                searchRequest.setKey(API_KEY);

                SearchListResponse searchResponse = searchRequest.execute();
                for (SearchResult result : searchResponse.getItems()) {
                    String description = result.getSnippet().getDescription();
                    descriptions.add(description != null ? description : "No description available");
                }
            } catch (IOException e) {
                logger.error("Error fetching descriptions for query: {}", query, e);
            }
            return descriptions;
        });
    }

    /**
     * Generates word statistics (word frequency) from video descriptions based on a search query.
     *
     * @param query the search query string
     * @return a {@code CompletableFuture} containing a map of words and their respective counts,
     * sorted in descending order of frequency
     */
    public CompletableFuture<Map<String, Integer>> fetchWordStatsByQuery(String query) {
        return fetchDescriptionsByQuery(query)
                .thenApply(descriptions -> {
                    Map<String, Integer> wordStats = new HashMap<>();
                    for (String description : descriptions) {
                        String[] words = description.toLowerCase()
                                .replaceAll("[^a-zA-Z\\s]", "")
                                .split("\\s+");

                        for (String word : words) {
                            if (!word.isEmpty()) {
                                wordStats.put(word, wordStats.getOrDefault(word, 0) + 1);
                            }
                        }
                    }

                    return wordStats.entrySet().stream()
                            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (e1, e2) -> e1,
                                    LinkedHashMap::new
                            ));
                });
    }

    /**
     * Retrieves recent videos from a channel's uploads playlist.
     *
     * @param playlistId the uploads playlist ID
     * @return a list of recent videos
     * @throws IOException if an error occurs during the API request
     */
    private List<Map<String, String>> fetchRecentVideos(String playlistId) throws IOException {
        YouTube.PlaylistItems.List request = youtubeService.playlistItems().list("snippet");
        request.setPlaylistId(playlistId);
        request.setMaxResults(10L);
        request.setKey(API_KEY);

        PlaylistItemListResponse response = request.execute();
        List<Map<String, String>> videos = new ArrayList<>();
        for (PlaylistItem item : response.getItems()) {
            Map<String, String> video = new HashMap<>();
            video.put("videoId", item.getSnippet().getResourceId().getVideoId());
            video.put("title", item.getSnippet().getTitle());
            video.put("thumbnailUrl", item.getSnippet().getThumbnails().getDefault().getUrl());
            videos.add(video);
        }
        return videos;
    }
}
