# JTermEmu
This is a terminal emulator written in Java, currently intended mainly as a proof of concept that it actually works in realtime at 60 fps, even on lower-spec hardware. I wrote the same program roughly 20 years ago, but lost its source code. So I rewrote it from scratch.

It has many features like overline, underline, double underline, slow blink, fast blink, color, thin, bold, blacken and various strike-out modes.

![Bildschirmfoto_2023-03-13_15-33-19](https://user-images.githubusercontent.com/52674537/225288193-80be9f46-ab4e-4deb-89f4-05631baa4b0e.png)

In this repo, you'll currently only find its Eclipse files until I've decided on a build tool chain.

I've added a couple of features from real terminal emulators, like Unicode input and output, some CSI / OSC sequence support on output (input still in the making), so it partially already works and can be used for some things.

![Bildschirmfoto_2023-03-15_11-47-00](https://user-images.githubusercontent.com/52674537/225286873-a2e6004e-69d0-4208-80d2-00bffe06d013.png)

https://www.youtube.com/watch?v=il3nkAb9zUI
