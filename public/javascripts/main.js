document.addEventListener("DOMContentLoaded", () => {
    const searchButton = document.getElementById("searchButton");
    const queryInput = document.getElementById("query");
    const resultsDiv = document.getElementById("results");

    const socket = new WebSocket("ws://localhost:9000/ws");

    socket.onopen = () => {
        console.log("WebSocket connection established.");
    };

    socket.onmessage = (event) => {
        console.log("Received data from server:", event.data);

        let data;
        try {
            data = JSON.parse(event.data); // Parse incoming JSON
        } catch (err) {
            console.error("Error parsing WebSocket message:", err);
            return;
        }

        if (data.query && data.overallSentiment) {
            // Handle the sentiment and query
            console.log("Handling query and sentiment:", data);
            renderQueryHeading(data.query, data.overallSentiment);
        }

        if (Array.isArray(data)) {
            // Handle the video results
            console.log("Processing video results:", data);
            renderVideoResults(data, queryInput.value.trim());
        } else if (data.profileData) {
            // Handle channel profile data
            console.log("Processing channel profile data:", data.profileData);
            renderChannelProfile(data.profileData);
        } else {
            console.warn("Unexpected response format or no results found.");
        }
    };

    socket.onerror = (error) => {
        console.error("WebSocket Error:", error);
    };

    socket.onclose = () => {
        console.log("WebSocket connection closed.");
    };

    searchButton.addEventListener("click", () => {
        const query = queryInput.value.trim();

        if (query) {
            const message = { action: "search", query: query };
            socket.send(JSON.stringify(message));
            console.log(`Search query sent: ${query}`);

            // Optional: Add a loading indicator
            const loadingIndicator = document.createElement("p");
            loadingIndicator.textContent = `Loading results for: ${query}`;
            loadingIndicator.id = `loading-${query}`;
            resultsDiv.prepend(loadingIndicator);
        } else {
            alert("Please enter a search query.");
        }
    });

    resultsDiv.addEventListener("click", (event) => {
        const target = event.target;
        if (target.tagName === "A" && target.dataset.channelId) {
            event.preventDefault();
            const channelId = target.dataset.channelId;

            const message = { action: "getChannelProfile", channelId: channelId };
            socket.send(JSON.stringify(message));
            console.log(`Fetching channel profile for: ${channelId}`);

            // Clear results and add a loading indicator for the channel profile
            resultsDiv.innerHTML = `<p>Loading channel profile...</p>`;
        }
    });

    function renderQueryHeading(query, sentiment) {
        const existingHeading = document.querySelector(`#heading-${CSS.escape(query)}`);
        if (!existingHeading) {
            const heading = document.createElement("h2");
            heading.textContent = `Results for: ${query} (Sentiment: ${sentiment})`;
            heading.id = `heading-${query}`;
            resultsDiv.prepend(heading);
        }
    }

    function renderVideoResults(videos, query) {
        const loadingIndicator = document.getElementById(`loading-${query}`);
        if (loadingIndicator) {
            loadingIndicator.remove();
        }

        const querySection = document.createElement("div");
        querySection.classList.add("query-section");

        videos.forEach((video, index) => {
            if (!video.title || !video.channelTitle || !video.defaultThumbnail) {
                console.warn(`Skipping incomplete video data for index ${index}:`, video);
                return;
            }

            const videoItem = document.createElement("div");
            videoItem.classList.add("video-item");

            const videoLink = video.videoId
                ? `<a href="https://www.youtube.com/watch?v=${video.videoId}" target="_blank">${video.title}</a>`
                : `${video.title} (No link available)`;

            videoItem.innerHTML = `
        <div class="video-details">
            <p><strong>Title:</strong> ${videoLink}</p>
            <p><strong>Channel:</strong> <a href="#" data-channel-id="${video.channelId}">${video.channelTitle}</a></p>
            <p><strong>Description:</strong> ${video.description || "No description available."}</p>
        </div>
        <div class="thumbnail">
            <img src="${video.defaultThumbnail}" alt="Thumbnail for ${video.title}" />
        </div>
        `;

            querySection.appendChild(videoItem);
        });

        if (querySection.childNodes.length > 0) {
            resultsDiv.prepend(querySection);
        } else {
            const noResultsMessage = document.createElement("p");
            noResultsMessage.textContent = "No valid videos found for your query.";
            resultsDiv.appendChild(noResultsMessage);
            console.warn("No valid video items to display.");
        }
    }

    function renderChannelProfile(profileData) {
        const channelProfileDiv = document.createElement("div");
        channelProfileDiv.classList.add("channel-profile");

        channelProfileDiv.innerHTML = `
        <h2>${profileData.title}</h2>
        <img src="${profileData.thumbnail}" alt="${profileData.title} Thumbnail" />
        <p><strong>Description:</strong> ${profileData.description}</p>
        <p><strong>Subscribers:</strong> ${profileData.subscriberCount}</p>
        <p><strong>Videos:</strong> ${profileData.videoCount}</p>
        <h3>Recent Videos:</h3>
        <ul>
            ${profileData.recentVideos
                .map(
                    (video) =>
                        `<li><a href="https://www.youtube.com/watch?v=${video.videoId}" target="_blank">${video.title}</a></li>`
                )
                .join("")}
        </ul>
        `;

        resultsDiv.innerHTML = "";
        resultsDiv.appendChild(channelProfileDiv);
    }
});
