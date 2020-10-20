
// Include library
var fs = require("fs");
var puppeteer = require("puppeteer");
var argv = require('minimist')(process.argv.slice(2));

// Parameter that pass from command line
var imageType = argv.format;
var imageWidth = argv.viewportWidth;
var imageHeight = argv.viewportHeight;
var svgFile = argv.url;
var pathToSaveFile = argv.imagePath;

(async () => {
	var page;
	var brower;
	try {
		var testContent = fs.readFileSync(svgFile, "utf-8");

		// Initial brower
		browser = await puppeteer.launch({ headless: true });

		// Initial new page from browser
		page = await browser.newPage();

		// Set width, height base on parameter
		await page.setViewport({width:imageWidth, height:imageHeight});

		// Load page and set content
		const loaded = page.waitForNavigation({
			waitUntil: 'load'
		});
		await page.setContent(testContent);
		await loaded

		// Option for screen to capture image
		var screenOptions = new Object();
		screenOptions.path = pathToSaveFile;

		// If image is jpeg
		if (imageType === "jpeg") {

			// Only set quality when image is jpeg (0-100)
			screenOptions.quality = 100;
		}
		screenOptions.type = imageType;

		// Screen shot
		await page.screenshot(screenOptions);
		//await page.close();
		await browser.close();


		// Write log success
		console.log("OK");
	} catch (error) {		

		// Write log error
		console.error(error);
	}

})();
