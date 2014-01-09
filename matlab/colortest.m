clc; clear;


globalthr = 0.2;


fname = '../OpenCVTest/images/balls1.jpeg';
img = double(imread(fname));
% img = [img(30:70, 10:50, :) img(75:115, 90:130, :) img(130:170, 105:150, :) img(45:85, 135:175, :)];
subplot(3, 1, 1);
image(uint8(img));

mincolor = min(img, [], 3);
normimg = cat(3, cat(3, img(:, :, 1) - mincolor, img(:, :, 2) - mincolor), img(:, :, 3) - mincolor);

% normimg = img;
% maxcolor = max(normimg, [], 3);
% normimg  = cat(3, cat(3, normimg(:, :, 1)./maxcolor, normimg(:, :, 2)./maxcolor), normimg(:, :, 3)./maxcolor); 

intimg = sum(normimg, 3);

%Reflection image. RGB+white => ([R G B] + 1)/2
rreflimg = normimg(:, :, 1) * 2 - 1;
greflimg = normimg(:, :, 2) * 2 - 1;
breflimg = normimg(:, :, 3) * 2 - 1;

subplot(3, 1, 2);
imagesc(uint8(normimg));

rval = 150;
gval = 30;
bval = 30;
color = [rval; gval; bval];
color = color/max(color);
totint = sum(color);

rdiff = normimg(:, :, 1)-color(1);
gdiff = normimg(:, :, 2)-color(2);
bdiff = normimg(:, :, 3)-color(3);

diffimg = sqrt(rdiff.*rdiff + gdiff.*gdiff + bdiff.*bdiff);


rrefldiff = rreflimg - color(1);
grefldiff = greflimg - color(2);
brefldiff = breflimg - color(3);

refldiffimg = sqrt(rrefldiff.*rrefldiff + grefldiff.*grefldiff + brefldiff.*brefldiff);

ball = double((diffimg < globalthr  & abs(intimg - totint) < 2*globalthr) ...
                | refldiffimg < 3*globalthr) ;


% %White
% rval = 250;
% gval = 250;
% bval = 250;
% color = [rval; gval; bval];
% color = color/max(color);
% 
% rdiff = normimg(:, :, 1)-color(1);
% gdiff = normimg(:, :, 2)-color(2);
% bdiff = normimg(:, :, 3)-color(3);
% diffimg = sqrt(rdiff.*rdiff + gdiff.*gdiff + bdiff.*bdiff);
% 
% ball = ball | double(diffimg < globalthr);

subplot(3, 1, 3);
imagesc(ball*255);
colormap(gray);
