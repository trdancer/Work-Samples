const user_poems = document.getElementById('user-poems');
const visitor_div = document.getElementById('visitor-poems-div');
const loading_div = document.getElementById('loading-div')
var global_counter;
const generated_poems_path = '/generated_poems/public/pdf/';
// var poem_names;
var mySwiper;
const scale = 4.0;
const desired_width = 300;
function createSwiper() {
    mySwiper = new Swiper('.swiper-container', {
        loop: false,
        allowTouchMove : false,
        simulateTouch: false,
        touchStartPreventDefault: false,
        pagination: {
            el: '.swiper-pagination',
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        }
    });
}
async function getNames() {
    var options = {
        method : 'GET'
    }
    // console.log('called getNames()');
    var url = 'https://strikethrough-score.org/api?action=poems';
    // var url = 'http://localhost:3000/api?action=poems';
    await fetch(url, options).then(response => {
        // console.log('completed promise 1');
        return response.json()}).then(function(data) {
            // console.log('completed promise 2')
            return new Promise((resolve, reject) => {
                poem_names = data.names;
                console.log(poem_names);
                if (poem_names != null) {
                    resolve(poem_names);
                }
                else {
                    reject();
                }
            }
            )}
            ).then(arr => {
                global_counter = arr.length;
                loadUserPoems(arr);
            })
    // loadUserPoems();
}
async function loadUserPoems(poem_names) {
    if (poem_names.length == 0) {
        removeLoader();
        return;
    }
    delete Array.prototype.removeIf;
    // console.log('called loadUserPoems');
    var counter = 0;
    var h = 0;
    await poem_names.forEach(async (n) => {
        counter++;
        let par = document.createElement('div');
        par.classList.add('user-poem');
        let bottom_div = document.createElement('div');
        let prev_button = document.createElement('img');
        prev_button.src = './media/caret-left.png';
        prev_button.classList.add('prev');
        prev_button.classList.add('pointer-hover');
        let next_button = document.createElement('img');
        next_button.src = './media/caret-right.png';
        next_button.classList.add('next');
        next_button.classList.add('pointer-hover');
        let outer_span = document.createElement('span');
        let page_num = document.createElement('span');
        page_num.classList.add('page-num');
        let page_count = document.createElement('span');
        page_count.classList.add('page-count');
        outer_span.appendChild(page_num);
        outer_span.appendChild(page_count);
        let page = document.createTextNode('Page: ');
        outer_span.insertBefore(page , page_num);
        let slash = document.createTextNode(' / ');
        outer_span.insertBefore(slash, page_count);
        bottom_div.appendChild(outer_span);
        var link = document.createElement('a');
        link.href = `/view_poem?poem_id=${n.substring(0, n.length- 4)}`;
        var c = document.createElement('canvas');
        link.appendChild(c);
        par.appendChild(link);
        par.appendChild(bottom_div);
        par.appendChild(prev_button);
        par.appendChild(next_button);
        user_poems.appendChild(par);
        await generatePDFViewer(n, par);
        if (counter % 2 != 0) {
            h += par.offsetHeight;
            // console.log(par.offsetHeight);
        }
    })
    // visitor_div.style.minHeight = `${h}px`;
}
function removeLoader() {
    loading_div.remove();
    user_poems.style.position = '';
    user_poems.style.display = '';
    // fade_out(loading_div);
    // loading_div.addEventListener(animationEvent, function(e) {
    //     e.preventdefault();
    //     loading_div.remove();
    // })
}
window.addEventListener('load', async function(e) {
    createSwiper();
    getNames();
})

function renderPage(info) {
  info.pageRendering = true;
  // Using promise to fetch the page
  info.pdfDoc.getPage(info.pageNum).then(function(page) {
    info.canvas.style.height = '90vh';
    var viewport = page.getViewport({scale: scale});
    info.canvas.height = viewport.height;
    info.canvas.width = viewport.width;
    // console.log('new scale: '+ new_scale+' || canvas height: '+info.canvas.height+' || canvas width: '+info.canvas.width);

    // Render PDF page into canvas context
    var renderContext = {
      canvasContext: info.ctx,
      viewport: viewport
    };
    var renderTask = page.render(renderContext);

    // Wait for rendering to finish
    renderTask.promise.then(function() {
      info.pageRendering = false;
      if (info.pageNumPending !== null) {
        // New page rendering is pending
        renderPage(info);
        info.pageNumPending = null;
      }
    });
  });

  // Update page counters
  info.pg.textContent = info.pageNum;
}
/**
 * Get page info from document, resize canvas accordingly, and render page.
 * @param num Page number.
 */


/**
 * If another page rendering in progress, waits until the rendering is
 * finised. Otherwise, executes rendering immediately.
 */
function queueRenderPage(info, num) {
  if (info.pageRendering) {
    info.pageNumPending = num;
  } else {
    renderPage(info);
  }
}

/**
 * Displays previous page.
 */
function onPrevPage(info) {
  if (info.pageNum <= 1) {
    return;
  }
  info.pageNum--;
  queueRenderPage(info, info.pageNum);
}

/**
 * Displays next page.
 */
function onNextPage(info) {
  if (info.pageNum >= info.pdfDoc.numPages) {
    return;
  }
  info.pageNum++;
  queueRenderPage(info, info.pageNum);
}

async function generatePDFViewer(file_name, par) {
    var url = `${generated_poems_path}${file_name}`;
    let c = par.querySelector('canvas');
    let p = par.querySelector('.page-num');
    var file_info = {
        pdfDoc: null,
        pageNum: 1,
        pageRendering: false,
        pageNumPending: null,
        scale: scale,
        canvas : c,
        pg : p
    }
    file_info.ctx =  file_info.canvas.getContext('2d');

/**
 * Asynchronously downloads PDF.
 */
    await pdfjsLib.getDocument(url).promise.then(function(pdfDoc_) {
        file_info.pdfDoc = pdfDoc_;
        par.querySelector('.page-count').textContent = file_info.pdfDoc.numPages;
        global_counter--;
        if (global_counter == 0) {
            removeLoader();
        }
        // Initial/first page rendering
        renderPage(file_info);
    }).catch((mes) => console.log(mes));
    par.querySelector('.next').addEventListener('click', () => {
        onNextPage(file_info)});
    par.querySelector('.prev').addEventListener('click', () => {
        onPrevPage(file_info)});
}
// pdfjsLib.GlobalWorkerOptions.workerSrc = 

// If absolute URL from the remote server is provided, configure the CORS
// header on that server.

// Loaded via <script> tag, create shortcut to access PDF.js exports.
// var pdfjsLib = https://unpkg.com/browse/pdfjs-dist@2.4.456/build/pdf.js;

// The workerSrc property shall be specified.
// pdfjsLib.GlobalWorkerOptions.workerSrc = '//unpkg.com/browse/pdfjs-dist@2.4.456/build/pdf.worker.js';

