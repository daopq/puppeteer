package mrhome.training;

import util.ChromeUtil;

import java.io.IOException;

public class MainExecute {

    private static final String NODEJS = "C:\\work\\nodejs\\puppeteer\\index.js";
    private static final String URL = "C:\\work\\nodejs\\puppeteer\\input\\documentImage.svg";
    private static final String WIDTH = "2243";
    private static final String HEIGHT = "1587";
    private static final String FORMAT = "jpeg";
    private static final String OUTPUT = "C:\\work\\nodejs\\puppeteer\\output\\documentImage.jpeg";

    public static void main(String[] args) throws InterruptedException {
        Process process = null;
        try {
            process = ChromeUtil.captureHtml(NODEJS, URL, WIDTH, HEIGHT, FORMAT, OUTPUT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }


    }

}
