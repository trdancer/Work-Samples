var save_pdf_input = document.getElementById('save_pdf_box'); 
var go_to_st_button = document.getElementById('go_to_st');
var scale = 3.5;
var current_step = 1;
var cur_stage = 1;
var entered_stage_2 = false, entered_stage_3 = false, entered_stage_4 = false, entered_stage_5 = false;
var how_to_div = document.getElementById('how-to');
var step_divs = document.querySelectorAll('.step');
var step_buttons = document.querySelectorAll('.step-button');
const step_1 = document.getElementById('step-1');
const step_2 = document.getElementById('step-2');
const step_3 = document.getElementById('step-3');
const jade_animation = document.getElementById('jade-animation-video');
const progress_bar = document.querySelector('.progress-bar');
const click_to_play = document.getElementById('ctp');
const example_poem_div = document.getElementById('example-poem');
const create_poem_link = document.getElementById('create_poem_link');
var watched_example = JSON.parse(window.localStorage.getItem('how_to_completed')) === true ? true : false;
const base_url = '/create_poem/';
var next_url = '/media/gallery/maxine/strikethrough_1/strikethrough_1_stage_3.jpg';
function addHideClass(e) {
    e.preventDefault();
    e.target.classList.add('hide');
    e.target.removeEventListener(animationEvent, addHideClass);
}
function showNextButton() {
    var b = document.querySelector('#step-2 .button-div .next');
    b.classList.remove('hide');
}
function updateLastImage(e) {
    e.preventDefault();
    e.target.classList.remove('cur');
    e.target.classList.add('nxt');
    switch (cur_stage) {
        case 2:
            e.target.src = '/media/gallery/maxine/strikethrough_1/strikethrough_1_stage_3.jpg';
            break;
        case 3:
            e.target.src = '/media/gallery/maxine/strikethrough_1/strikethrough_1_stage_4.jpg';
            break;
        case 4:
            e.target.src = '/media/gallery/maxine/strikethrough_1/strikethrough_1_stage_5.jpg';
            break;
        }
    e.target.removeEventListener(animationEvent, updateLastImage);

}
function updateNextImage(e) {
    e.preventDefault();
    e.target.classList.remove('nxt');
    e.target.classList.add('cur');
    e.target.removeEventListener(animationEvent, updateNextImage);
}
function flipForward() {
    cur_stage++;
    var current_img = document.querySelector('.cur');
    var next_img = document.querySelector('.nxt');
    current_img.classList.remove('flipforward');
    current_img.classList.add('flipbackward');
    next_img.classList.remove('flipbackward');
    next_img.classList.add('flipforward');
    current_img.addEventListener(animationEvent, updateLastImage);
    next_img.addEventListener(animationEvent, updateNextImage);

}
function updateProgressBar() {
    progress_bar.style.width = `${(jade_animation.currentTime / jade_animation.duration) * 100}%`;
}
function flipCards(e) {
    e.preventDefault();
    var video_time = jade_animation.currentTime;
    updateProgressBar()
    if ( jade_animation.duration - video_time <= 5) {
        showNextButton();
        watched_example = true;
        window.localStorage.setItem('how_to_completed', true);
    }
    if (video_time >= 102 && entered_stage_5 == false) {
        entered_stage_5 = true;
        //stage 5
        flipForward();
    }
    else if (video_time >= 71 && entered_stage_4 == false) {
        entered_stage_4 = true;
        //stage 4
        flipForward();
        // document.querySelector('.nxt').src = '/media/gallery/maxine/strikethrough_1/strikethrough_1_stage_5.jpg';
    }
    else if (video_time >= 60 && entered_stage_3 == false) {
        entered_stage_3 = true;
        //stage 3
        flipForward();
        // next_url = '/media/gallery/maxine/strikethrough_1/strikethrough_1_stage_4.jpg';
    }
    else if (video_time >= 15 && entered_stage_2 == false) {
        entered_stage_2 = true;
        flipForward();
    }
    if (click_to_play && video_time >=1) {
        fade_out(click_to_play);
        click_to_play.addEventListener(animationEvent, function(e) {
            click_to_play.remove();
        })
    }

}
function viewStep(e) {
	// e.preventDefault();
	var tar = e.target;
	var forward = tar.classList.contains('next');
	var current_step_div = step_divs[current_step-1];
	if (forward) {
        if (current_step == 2 && watched_example == false) {
            return;
        }
        fadeOut(current_step_div);
        current_step_div.addEventListener(animationEvent, addHideClass);
		fadeIn(step_divs[current_step]);
		step_divs[current_step].classList.remove('hide');
        step_divs[current_step].scrollIntoView(true);
        how_to_div.style.minHeight = `${step_divs[current_step].offsetHeight}px`;
		// console.log('going forward');
		current_step++;
	}
	else {
        fadeOut(current_step_div);
        current_step_div.addEventListener(animationEvent, addHideClass);
		fadeIn(step_divs[current_step-2]);
		step_divs[current_step-2].classList.remove('hide');
        step_divs[current_step-2].scrollIntoView(true);
        how_to_div.style.minHeight = `${step_divs[current_step-2].offsetHeight}px`;
		// console.log('going back');
        current_step--;
	}
    jade_animation.currentTime = 0;
    var imgs = example_poem_div.querySelectorAll('img');
    setTimeout(() => {
        for (i=0 ; i< imgs.length; i++) {
            img = imgs[i];
            img.src = `/media/gallery/maxine/strikethrough_1/strikethrough_1_stage_${i+1}.jpg`;
            img.classList.remove('flipbackward');
            img.classList.remove('flipforward');
            if (i == 1) {
                img.classList.add('nxt');
                img.classList.remove('cur');
            }
            else {
                img.classList.add('cur');
                img.classList.remove('nxt');
            }
        }
    }, 3000);
}

window.onload = () => {
    step_buttons.forEach(b => {b.addEventListener('click', viewStep)});
    if (watched_example === true) {
        showNextButton();
    }
    if (jade_animation.readyState >= 3) {
        jade_animation.addEventListener('click', videoPlay);
    }
    else {
        jade_animation.addEventListener('canplay', videoReady);
    }
    how_to_div.style.minHeight = `${step_divs[0].offsetHeight}px`;
    jade_animation.addEventListener('timeupdate', flipCards);
    save_pdf_input.addEventListener('change', function(e) {
    	e.preventDefault();
    	if (save_pdf_input.checked) {
    		create_poem_link.href = `${base_url}?save_pdf=true`;
    	}
    	else {
    		create_poem_link.href= `${base_url}?save_pdf=false`;
    	}
    })
}
// go_to_step_2_button.addEventListener('click', view_step);