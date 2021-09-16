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
    if (state.winnerNumber >= state.raffle.winners.length) {
        // No more winners
        showNoWinnerFound();
        return;
    }
    // Get next winner
    const winner = state.raffle.winners[state.winnerNumber];
    // Get next winner tweet id
    let id = winner.tweetUrl;
    const lastIndexOf = id.lastIndexOf('/');
    id = id.substr(lastIndexOf + 1);

    // Ensure winner panel it shown
    document.getElementById('home').classList.add('hidden');
    document.getElementById('winner').classList.remove('hidden');
    // Update winner panel
    // document.getElementById('winner-name').innerHTML = winner.name + '(<cite>@' + winner.screenName + '</cite>)';
    const tweetElement = document.getElementById('tweet');
    while (tweetElement.firstChild) {
        tweetElement.removeChild(tweetElement.firstChild);
    }
    twttr.widgets.createTweet(id, tweetElement, {
        conversation: "none",
        align: "center",
        lang: "fr",
        dnt: true
    });
}

function showNoWinnerFound() {

}