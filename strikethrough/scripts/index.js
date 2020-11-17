const welcome_section = document.getElementById('welcome-section');
var welcome_message = document.querySelector('.welcome-message');
var welcome_videos = document.querySelectorAll('#welcome-section .video-wrapper video');
var progress_bars = document.querySelectorAll('#welcome-section .video-wrapper .progress-bar');

const hide_bar = document.getElementById('hide-button');
const hide_text = document.getElementById('hide-text');
const hide_show_arrow = document.getElementById('hide-show-arrow');
const main_section = document.getElementById('main-section');
var main_vids = main_section.querySelectorAll('video');

const replay_button = document.querySelector('.replay-button');

const cookie_banner = document.querySelector('.cookie-banner');
const cookie_ok = document.querySelector('#accept-cookies-box');
const cookie_x_button = document.getElementById('x-button');
const cookie_remover = document.getElementById('cookie-remover');
const glob_time_needed = 15;
var intro_started = false;
var no_x = false;
var performance_unlocked = false, outro_unlocked = false, end_unlocked = false, perf_loaded = false, outro_loaded = false;
if (visited == false) {
	console.log('cookies have been accepted, removing banner');
	cookie_banner.classList.remove('hide');
}
if (viewed_home_videos == false) {
	console.log('showing videos')
	welcome_section.classList.remove('hide');
}
else {
	replay_button.classList.remove('hide');
	welcome_message.remove();
	enableVideoPlay();
}

function fadeOutPlayIcon(e) {
	var tar = e.target;
	var icon = tar.parentNode.querySelector('.play-icon');
	icon.classList.add('quick-fadeout');
	icon.addEventListener(animationEvent, (e) => {
		e.target.remove();
	});
	tar.removeEventListener('play', fadeOutPlayIcon);
}
function enableVideoPlay() {
	main_vids.forEach((vid) => {
		if (vid.readyState >= 3) {
			vid.addEventListener('click', videoPlay);
		}
		else {
			vid.addEventListener('canplay', videoReady);
		}
		vid.addEventListener('play', fadeOutPlayIcon);
		var icon = vid.parentNode.querySelector('.play-icon');
		icon.addEventListener('click', (e) => {
			e.preventDefault();
			var tar = e.target;
			var v = tar.parentNode.querySelector('video');
			v.play();
		})
	});
}
function updateProgressBar(video) {
	var bar = video.nextElementSibling;
	bar.style.width = `${(video.currentTime / video.duration) * 100}%`;
}
function getTimeleft(video) {
	return (((glob_time_needed - video.currentTime) <= 0) ? 0 : Math.ceil(glob_time_needed - video.currentTime));
}
// function devGetTimeLeft(video) {
// 	return ((( - video.currentTime) <= 0) ? 0 : Math.ceil(glob_time_needed - video.currentTime));
// }
// function adjustRowHeight(left) {
// 	var vid_el = left ? document.querySelector('#left-column .video-wrapper') : document.querySelector('#right-column .video-wrapper')
// 	var vid_el_height = vid_el.offsetHeight+vid_el.offsetTop;
// 	welcome_section.style.height = `${vid_el_height+20}px`;
// 	document.documentElement.style.setProperty('--welcome-height', `${vid_el_height+20}px`)
// }
// function slideHideBar(e) {
// 	e.preventDefault();
// 	hide_show_arrow.classList.toggle('rotate');
// 	if (welcome_section.classList.contains('hide-bar')) {
// 		welcome_section.classList.remove('hide-bar');
// 		welcome_section.classList.add('show-bar');
// 		hide_text.textContent = 'Hide';
// 	}
// 	else {
// 		welcome_section.classList.add('hide-bar');
// 		welcome_section.classList.remove('show-bar');
// 		hide_text.textContent = 'Show';
// 	}
// }
function clickOutside(event, element, func) {
	let tar = event.target; // clicked element
	if (cookie_banner) {
		event.preventDefault();
	}
    do {
        if (tar == element) {
            return;
        }
        // Go up the DOM
        tar = tar.parentNode;
    } while (tar);
    func(event);
}
function loadVideo(v, source_path) {
	v.querySelector('source').src = source_path;
	v.load();
}
function removeWelcomeMessage(e) {
	welcome_message.classList.remove('fadein-2');
	welcome_message.classList.add('quick-fadeout-2');
	welcome_message.addEventListener(animationEvent, function(e) {
		welcome_message.remove();
	})
}
function removeCookieBanner(e) {
	cookie_banner.classList.add('slidedown');
	window.localStorage.setItem('visited', true);
	cookie_ok.removeEventListener('click', removeCookieBanner);
	cookie_x_button.removeEventListener('click', removeCookieBanner);
	cookie_banner.addEventListener(animationEvent, function(e) {
		e.preventDefault();
		e.target.remove();
	})
}
function resetVids(e) {
	// e.preventDefault();
	var tar = e.target;
	performance_unlocked = false;
	outro_unlocked = false;
	end_unlocked = false;

	welcome_videos.forEach((v) => {
		v.currentTime = 0;
	});
	if (no_x == false) {
		var x = document.createElement('img');
		x.src = '/media/x_button.png';
		x.id = 'welcome-x';
		x.addEventListener('click', unlockMainSection);
		welcome_section.querySelector('.welcome-bg').appendChild(x);
		no_x = true;
	}
	//add escape button press to exit
	welcome_section.classList.remove('fadeout-2');
	welcome_section.classList.add('fadein-2');
	welcome_section.classList.remove('hide');
}
function viewIntro(e) {
	e.preventDefault();
	var tar = e.target;
	updateProgressBar(tar);
	var time_needed = getTimeleft(tar);
	if (time_needed <= 5 && !perf_loaded) {
		loadVideo(welcome_videos[1], '/media/homepage/intro_outro_homepage.mp4');
		perf_loaded = true;
	}
	if (time_needed === 0 && !performance_unlocked) {
		performance_unlocked = true;
		welcome_videos[1].parentNode.classList.add('fadein-2');
		welcome_videos[1].parentNode.classList.remove('transparent');
		welcome_videos[1].classList.remove('no-pointer');
		welcome_videos[1].addEventListener('click', videoPlay);
		welcome_videos[1].addEventListener('timeupdate', viewPerf);
	}
}
async function viewPerf(e) {
	e.preventDefault();
	var tar = e.target;
	updateProgressBar(tar);
	var time_needed = getTimeleft(tar);
	if (time_needed <= 5 && !outro_loaded) {
		loadVideo(welcome_videos[2], '/media/homepage/outro_homepage.mp4');
		outro_loaded = true;
	}
	if (time_needed === 0 && !outro_unlocked) {
		outro_unlocked = true;
		welcome_videos[2].parentNode.classList.add('fadein-2');
		welcome_videos[2].parentNode.classList.remove('transparent');
		welcome_videos[2].classList.remove('no-pointer');
		welcome_videos[2].addEventListener('click', videoPlay);
		welcome_videos[2].addEventListener('timeupdate', viewOutro);
	}
}
function viewOutro(e) {
	e.preventDefault();
	var tar = e.target;
	updateProgressBar(tar);
	if (tar.ended == true && !end_unlocked) {
		end_unlocked = true;
		// tar.removeEventListener('timeupdate', viewOutro);
		unlockMainSection();
	}
}
function unlockMainSection() {
	window.localStorage.setItem('viewed_home_videos', true);
	unlockMenu();
	replay_button.classList.remove('hide');
	//TO DO
	if (no_x == false) {
		var x = document.createElement('img');
		x.src = '/media/x_button.png';
		x.id = 'welcome-x';
		x.addEventListener('click', unlockMainSection);
		welcome_section.querySelector('.welcome-bg').appendChild(x);
		no_x = true;
	}
	//temporary, add more, better effect later
	welcome_section.classList.add('fadeout-2');
	welcome_section.addEventListener(animationEvent, addHideClass);
	//show main_section
	enableVideoPlay();
}
window.onload = () => {
	if (welcome_videos[0].readyState >= 3) {
		welcome_videos[0].addEventListener('click', videoPlay);
		welcome_videos[0].addEventListener('timeupdate', viewIntro);
	}
	else {
		welcome_videos[0].addEventListener('canplay', function(e) {
			videoReady(e);
			e.target.addEventListener('timeupdate', viewIntro);
		});
	}
	welcome_videos[0].addEventListener('play', removeWelcomeMessage);
	cookie_ok.addEventListener('click', removeCookieBanner);
	welcome_section.addEventListener('click', (e) => {
		removeCookieBanner()
	});
	replay_button.addEventListener('click', resetVids);
}