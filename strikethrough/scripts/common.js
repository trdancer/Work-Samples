const nav = document.querySelector('.page-header');
const inners = document.querySelectorAll('.inner');
const outers = document.querySelectorAll('.outer');
var is_in = false;
// export { viewed_home_videos, visited, addHideClass, fade_in, fade_out, long_fade_in, long_fade_out, fadeoutElements, fadeinElements, 
// 	media_play_toggle, video_ready, video_play, play_mouse_over, pause_mouse_out, unlockMenu, load, animationEvent};
var viewed_home_videos = window.localStorage.getItem('viewed_home_videos') ? JSON.parse(window.localStorage.getItem('viewed_home_videos')) : false;
var visited = window.localStorage.getItem('visited') ? JSON.parse(window.localStorage.getItem('visited')) : false;
function whichAnimationEvent() {
    var t;
    var el = document.createElement('fakeelement');
    var animations = {
      'animation':'animationend',
      'OAnimation':'oAnimationEnd',
      'MozAnimation':'animationend',
      'WebkitAnimation':'webkitAnimationEnd'
    }
    for (var a in animations){
        if( el.style[a] !== undefined ){
            return animations[a];
        }
    }
}
const animationEvent = whichAnimationEvent();
function addHideClass(e) {
	e.target.classList.add('hide');
	e.target.removeEventListener(animationEvent, addHideClass);
}
function fadeIn(element) {
	element.classList.remove('fadeout');
	element.classList.add('fadein');
}
function fadeOut(element) {
	element.classList.remove('fadein');
	element.classList.add('fadeout');
}
function longFadeIn(element) {
	element.classList.remove('long-fadeout');
	element.classList.add('long-fadein');
}
function longFadeOut(element) {
	element.classList.remove('long-fadein');
	element.classList.add('long-fadeout');
}
function fadeoutElements(els) {
	els.forEach(e => {
		// console.log('fading out ' + e.innerHTML);
		e.classList.remove('quick-fadein');
		e.classList.add('quick-fadeout');
	});
}
function fadeinElements(els) {
	els.forEach(e => {
		// console.log('fading in ' + e.innerHTML);
		e.classList.remove('quick-fadeout');
		e.classList.add('quick-fadein');
	});
}
function mediaPlayToggle(media) {
	if (media.paused) {
		media.play();
	}
	else {
		media.pause();
	}
}
function videoReady(e) {
	e.preventDefault();
	e.target.addEventListener('click', videoPlay);
	e.target.removeEventListener('canplay', videoReady);
}
function videoPlay(e) {
	e.preventDefault();
	var tar = e.target;
	mediaPlayToggle(tar);
}
// function play_mouse_over(e) {
// 	e.preventDefault();
// 	var tar = e.target;
// 	tar.play();
// }
// function pause_mouse_out(e) {
// 	e.preventDefault();
// 	var tar = e.target;
// 	tar.pause();
// }
function unlockMenu() {
	inners.forEach(e => e.style.display = 'inline-block');
	outers.forEach(e => e.style.display = 'inline-block');
	fadeinElements(inners);
	setTimeout(()=> {fadeinElements(outers)}, 400);
	setTimeout(()=>{fadeoutElements(outers)}, 5000);
	setTimeout(() => {fadeoutElements(inners)}, 5600);
	nav.addEventListener('mouseenter', async function(e){
		e.preventDefault();
		is_in = true;
		// console.log('moused over');
		fadeinElements(inners);
		setTimeout(function() {
			if (is_in) {
				fadeinElements(outers);
			}
		}, 200);
	});
	nav.addEventListener('mouseleave', async function(e){
		e.preventDefault();
		is_in = false;
		// console.log('mouse exit');
		fadeoutElements(outers);
		setTimeout(function() {
			fadeoutElements(inners);
		}, 300);
	});

}
if (viewed_home_videos) {
	unlockMenu();
}