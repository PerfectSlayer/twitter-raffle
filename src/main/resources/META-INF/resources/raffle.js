let state = {
    raffle: {
        id: 0,
        winners: []
    },
    winnerNumber: -1
};

function performRaffle() {
    const speaker = document.getElementById('speaker').value;
    fetch("/api/raffle/", {
        method: "POST",
        body: JSON.stringify(speaker),
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => response.json())
        .then(raffle => showWinners(raffle))
}

function showWinners(raffle) {
    state.raffle = raffle;
    state.winnerNumber = -1;
    showNextWinner();
}

function showNextWinner() {
    state.winnerNumber++;
    if (state.winnerNumber < state.raffle.winners.length -1) {
        showNextWinnerButton();
    } else {
        showNoMoreWinnerButton();
        if (state.winnerNumber < state.raffle.winners) {
            return;
        }
    }
    // Get next winner
    const winner = state.raffle.winners[state.winnerNumber];
    // Ensure winner container is shown
    document.getElementById('winner').classList.remove('d-none');
    // Remove previous tweet
    const tweetElement = document.getElementById('tweet');
    while (tweetElement.firstChild) {
        tweetElement.removeChild(tweetElement.firstChild);
    }
    // Load winner tweet
    twttr.widgets.createTweet(winner.tweetId, tweetElement, {
        conversation: "none",
        align: "center",
        lang: "fr",
        dnt: true
    }).then(() => scrollToBottom());
}

function showNextWinnerButton() {
    document.getElementById('nextWinnerButton').classList.remove('d-none');
    document.getElementById('noMoreWinner').classList.add('d-none');
}

function showNoMoreWinnerButton() {
    document.getElementById('nextWinnerButton').classList.add('d-none');
    document.getElementById('noMoreWinner').classList.remove('d-none');
}

function scrollToBottom() {
    window.scrollTo(0, document.body.scrollHeight);
}
