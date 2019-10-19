let globalWinners = [];
let globalCurrentWinner = -1;

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
        // No more winners
        showNoWinnerFound();
        return;
    }
    // Get next winner
    const winner = globalWinners[globalCurrentWinner];
    const tweetUrl = winner.tweetUrl;
    // Request embedded tweet HTML code
    const url = "https://cors-anywhere.herokuapp.com/publish.twitter.com/oembed?url=" + encodeURI(tweetUrl) + "&omit_script=true";
    const xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'json';
    xhr.onload = function () {
        var status = xhr.status;
        if (status === 200) {
            console.log(xhr.response);
            // Ensure winner panel it shown
            document.getElementById('home').classList.add('hidden');
            document.getElementById('winner').classList.remove('hidden');
            // Update winner panel
            document.getElementById('winner-name').innerHTML = winner.name + '(<cite>@' + winner.screenName + '</cite>)';
            const tweetElement = document.getElementById('tweet');
            tweetElement.innerHTML = xhr.response.html;
            // Load widget
            twttr.widgets.load(tweetElement);
        } else {
            console.log('Status: ' + status);
        }
    };
    xhr.send();
}

function showNoWinnerFound() {

}