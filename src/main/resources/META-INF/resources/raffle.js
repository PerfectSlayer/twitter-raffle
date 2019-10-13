var globalWinners = [];
var globalCurrentWinner = -1;

function performRaffle() {
    const speaker = document.getElementById('speaker').value;
    fetch("/raffle?speaker=" + speaker)
        .then(response => response.json())
        .then(winners => showWinners(winners));
}

function showWinners(winners) {
    globalWinners = winners;
    showNextWinner();
}

function showNextWinner() {
    globalCurrentWinner++;
    if (globalCurrentWinner >= globalWinners.length) {
        // Pas d'autres gagnants
        showNoWinnerFound();
        return;
    }
    // Get next winner
    const winner = globalWinners[globalCurrentWinner];
    const tweetUrl = winner.tweetUrl;
    // Request embeded tweet HTML code
    const url = "https://cors-anywhere.herokuapp.com/publish.twitter.com/oembed?url=" + encodeURI(tweetUrl);
    var xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'json';
    xhr.onload = function () {
        var status = xhr.status;
        if (status === 200) {
            console.log(xhr.response);
            // Ensure winner panel it shown
            document.getElementById("home").classList.add("hidden");
            document.getElementById("winner").classList.remove("hidden");
            // Update winner panel
            document.getElementById('winner-name').innerHTML = winner.name + "(<cite>@" + winner.screenName + "</cite>)";
            document.getElementById('tweet').innerHTML = xhr.response.html;
        } else {
            console.log("Status: " + status);
        }
    };
    xhr.send();
}

function showNoWinnerFound() {

}