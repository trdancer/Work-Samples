var scene_index = 0;
var dot_position = 0;
var intervalId;
const scenes = document.querySelectorAll('.scene');
const scene_medias = document.querySelectorAll('.scene-media');
const scene_texts = document.querySelectorAll('.scene-text');
const next_button= document.getElementById('next-scene');
const prev_button = document.getElementById('previous-scene');
const main_section = document.getElementById('main-section');
const page_dot = document.querySelector('.dot.active');
const loading_gif = document.getElementById('loading');
var adjusted_height = false;
var validator = [false, false, false, false, false, false, false];
var scenes_visited = [false, false, false, false, false, false, false, false];
const first_scene_media = scene_medias[0].querySelectorAll('img, video');
function utilPrevTop(e) {
	scenes[scene_index].classList.remove('top');
	scene_index--;
	scenes[scene_index].classList.add('top');
	e.target.removeEventListener(animationEvent, utilPrevTop);
}
function utilNextTop(e) {
	scenes[scene_index].classList.remove('top');
	scene_index++
	scenes[scene_index].classList.add('top');
	e.target.removeEventListener(animationEvent, utilNextTop);
}
function topFlipLeftDone(e) {
	e.target.classList.remove('flip-to-left');
	e.target.removeEventListener(animationEvent, topFlipLeftDone)
}
function bottomFlipLeftDone(e) {
	e.target.classList.remove('flip-to-left-2');
	e.target.removeEventListener(animationEvent, bottomFlipLeftDone)
}
function topFlipRightDone(e) {
	e.target.classList.remove('flip-to-right');
	e.target.removeEventListener(animationEvent, topFlipRightDone)
}
function bottomFlipRightDone(e) {
	e.target.classList.remove('flip-to-right-2');
	e.target.removeEventListener(animationEvent, bottomFlipRightDone)
}
function flipLeft(e) {
	e.preventDefault();
	if (scene_index == scenes.length-1) {
		return;
	}
	var cur_scene = scenes[scene_index];
	var next_scene = scenes[scene_index+1];
	next_scene.classList.remove('hide');
	var next_text = next_scene.querySelector('.scene-text');
	var next_media = next_scene.querySelector('.scene-media');
	var cur_media = cur_scene.querySelector('.scene-media');
	if (scenes_visited[scene_index+1] == false) {
		var media = next_media.querySelectorAll('img, video');
		var media_height = 15;
		media.forEach((m) => {
			// console.log('there is a piece of media with height '+m.scrollHeight);
			media_height += (m.scrollHeight + 14);
		});
		max_height = Math.max(next_text.scrollHeight, media_height);
		next_text.style.height = `${max_height}px`;
		next_media.style.height = `${max_height}px`;
		scenes_visited[scene_index+1] = true;
	}
	cur_media.classList.remove('flip-to-right');
	cur_media.classList.remove('flip-to-right-2');
	cur_media.classList.add('flip-to-left');
	cur_media.addEventListener(animationEvent, topFlipLeftDone);
	next_text.classList.remove('flip-to-right');
	next_text.classList.remove('flip-to-left');
	next_text.classList.remove('facedown');
	cur_scene.classList.remove('top');
	cur_scene.classList.remove('bring-to-top');
	cur_scene.classList.add('bring-to-bottom');
	next_scene.classList.remove('bring-to-bottom');
	next_scene.classList.add('bring-to-top');
	next_scene.classList.remove('bottom');
	next_text.classList.add('flip-to-left-2');
	dot_position += 29.75;
	page_dot.style.left = `${dot_position}px`;
	next_text.addEventListener(animationEvent, bottomFlipLeftDone);
	cur_scene.addEventListener(animationEvent, addHideClass);
	next_text.scrollIntoView(true);
	scene_index++;
	if (scene_index == scenes.length-1) {
		fadeOut(next_button);
		next_button.addEventListener(animationEvent, addHideClass);
	}
	if (scene_index == 1) {
		fadeIn(prev_button);
		prev_button.classList.remove('hide');
	}
	var m = scenes[scene_index].querySelector('.scene-media').scrollHeight;
	main_section.style.height = (m+40).toString()+'px';
	// console.log(scene_index);
}
function flipRight(e) {
	e.preventDefault();
	if (scene_index == 0) {
		return;
	}
	var cur_scene = scenes[scene_index];
	var prev_scene = scenes[scene_index-1];
	prev_scene.classList.remove('hide');
	var cur_text = cur_scene.querySelector('.scene-text');
	var prev_media = prev_scene.querySelector('.scene-media');

	cur_text.classList.remove('flip-to-left-2');
	cur_text.classList.remove('flip-to-left');
	cur_text.classList.remove('flip-to-left-2')
	cur_text.classList.add('flip-to-right');
	cur_text.addEventListener(animationEvent, topFlipRightDone);
	prev_media.classList.remove('flip-to-left-2')
	prev_media.classList.remove('flip-to-left');
	prev_media.classList.remove('flip-to-right');
	cur_scene.classList.remove('bring-to-top')
	cur_scene.classList.add('bring-to-bottom')
	prev_scene.classList.remove('bring-to-bottom')
	prev_scene.classList.add('bring-to-top');
	prev_media.classList.add('flip-to-right-2');
	dot_position -= 29.75;
	page_dot.style.left = `${dot_position}px`;
	prev_media.addEventListener(animationEvent, bottomFlipRightDone);
	cur_scene.addEventListener(animationEvent, addHideClass);
	prev_media.scrollIntoView(true);
	scene_index--;
	if (scene_index == 0) {
		fadeOut(prev_button)
		prev_button.addEventListener(animationEvent, addHideClass);
	}
	if (scene_index == scenes.length-2) {
		fadeIn(next_button);
		next_button.classList.remove('hide');
	}
	var m = scenes[scene_index].querySelector('.scene-media').scrollHeight;
	main_section.style.height = (m+40).toString()+'px';
	// console.log(scene_index);
}
function mediaLoaded() {
	var counter = 0;
	for (i=0;i < first_scene_media.length; i++) {
		if (validator[i] == true) {
			counter++;
			break;
		}
		let m = first_scene_media[i];
		if (m.tagName === 'IMG' && m.complete == true) {
			validator[i] = true;
		}
	}
	if (counter === 7) {
		clearInterval(intervalId);
		findMaxHeight()
	}
}
function findMaxHeight() {
	var media_height = 15;
	first_scene_media.forEach((m) => {
		media_height += (m.scrollHeight + 14);
	})
	max_height = Math.max(scene_texts[0].scrollHeight, media_height) + 30;
	// console.log(max_height);
	scene_texts[0].style.height = `${max_height}px`;
	scene_medias[0].style.height = `${max_height}px`;
	main_section.style.height = `${max_height+70}px`;
	// document.querySelector('body').style.height = `${max_height+70}px`;
	adjusted_height = true;
	loading_gif.classList.add('quick-fadeout');
	loading_gif.addEventListener(animationEvent, (e)=> {
		loading_gif.remove();
	})
}
window.onload =  async () => {
	intervalId = setInterval(mediaLoaded, 500);
	scenes[0].querySelectorAll('video').forEach((v) => {
		v.addEventListener('canplay', (e) => {
			var i = Array.prototype.indexOf.call(e.target.parentNode.children, e.target);
			validator[i] = true;
		})
	})
	setTimeout(() => {
		if (adjusted_height == false) {
			clearInterval(intervalId);
			findMaxHeight();
		}
	}, 6000);
	next_button.addEventListener('click', flipLeft);
	prev_button.addEventListener('click', flipRight);
}