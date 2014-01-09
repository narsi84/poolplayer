function tabletest
clear;

global img localthr globalthr whiteRatioThreshold intensityThreshold

fname = '../OpenCVTest/images/balls1.jpeg';
img = double(imread(fname));
% img = img(25:75, 125:200, :);

globalthr = 0.2;
localthr = .1;
whiteRatioThreshold = 2.5;
intensityThreshold = 150;

rval = 250;
gval = 130;
bval = 10;
color = [rval; gval; bval];

subplot(1, 2, 1);
image(uint8(img));

ball = threshold(img, color);
subplot(1, 2, 2);
image(ball*255);

dim = [size(ball, 1) size(ball, 2)];

pause on;

% Grow color regions
validPixelList = find(ball);
[toGrowX, toGrowY] = ind2sub(dim, validPixelList);
ball = regionGrow(ball, toGrowX, toGrowY, color, @testNeighbor, 1);

% Filter noise
mask = imopen(ball>0);
image(mask*255);

%Grow white regions
validPixelList = find(mask);
[toGrowX, toGrowY] = ind2sub(dim, validPixelList);
ball = regionGrow(ball, toGrowX, toGrowY, color, @testWhite, 2);

return

%% Color threshold
function ball = threshold(img, color)
global globalthr

maxcolor = max(img, [], 3);

normimg  = cat(3, cat(3, (img(:, :, 1)./maxcolor), img(:, :, 2)./maxcolor), img(:, :, 3)./maxcolor);

color = color/max(color);

rdiff = normimg(:, :, 1)-color(1);
gdiff = normimg(:, :, 2)-color(2);
bdiff = normimg(:, :, 3)-color(3);
diffimg = sqrt(rdiff.*rdiff + gdiff.*gdiff + bdiff.*bdiff);

ball = double(diffimg < globalthr);

return

%% Region growing
function ball = regionGrow(ball, toGrowX, toGrowY, color, tfunc, N)

while ~isempty(toGrowX)
    disp(['To grow: ' num2str(length(toGrowX))]);
    candidatePixelList = [];
    for i=1:length(toGrowX)
        x = toGrowX(i); y = toGrowY(i);
        
        for n=1:N
            row = x-n; col = y-n;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
            
            
            row = x-n; col = y;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
            
            row = x-n; col = y+n;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
            
            row = x; col = y-n;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
            
            row = x; col = y+n;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
            
            row = x+n; col = y-n;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
            
            row = x+n; col = y;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
            
            row = x+n; col = y+n;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
            
            row = x-n; col = y-n;
            ret =  tfunc(ball, row, col, x, y, color);
            if ret > -1
                candidatePixelList = [candidatePixelList; [row col]];
                ball(row, col) = ret;
            end
        end
        
    end
    
    imagesc(ball);
    pause(.1);
    
    if isempty(candidatePixelList)
        break;
    end
    toGrowX = candidatePixelList(:, 1);
    toGrowY = candidatePixelList(:, 2);
end
return


%% Test neighboor for color similarity
function ret = testNeighbor(ball, row, col, x, y, color)
global img localthr globalthr

ret = -1;

% If the neighbor is similar to this pixel but not too much from the actual color and was not identified
% before, add it to candidate list

if row == 0 || col == 0 || row > size(img, 1) || col > size(img, 2)
    return
end

neighborVal = img(row, col, :);
neighborVal = neighborVal(:);

thisVal = img(x, y, :);
thisVal = thisVal(:);

if norm(neighborVal - thisVal) < localthr && ball(row, col) == 0   ...
        && norm(neighborVal - color) < globalthr + 1*localthr
    ret = 255-norm(neighborVal - color);
end

return

%% Test neighbor for white
function ret = testWhite(ball, row, col, x, y, color)
global img whiteRatioThreshold intensityThreshold

ret = -1;
if row == 0 || col == 0 || row > size(img, 1) || col > size(img, 2)
    return
end

neighborVal = img(row, col, :);
neighborVal = neighborVal(:);

if sum(neighborVal/max(neighborVal)) >= whiteRatioThreshold  && ball(row, col) == 0   ...
        && mean(neighborVal) >= intensityThreshold
    ret = 1;
%     ret = mean(neighborVal);
end

return


%% imopen
function res = imopen(img)
eroded = imerode(img);
res = imdilate(eroded);
return

%% imclose
function res = imclose(img)
dilated = imdilate(img);
res = imerode(dilated);
return

%% imdilate
function res = imdilate(img)
padded = zeros(size(img)+2);
padded(2:end-1, 2:end-1) = img;

indx = find(padded);
siz = [size(padded, 1), size(padded, 2)];

res = zeros(siz);
for i=1:length(indx)
    [x, y] = ind2sub(siz, indx(i));
    res(x-1, y) = 1;
    res(x, y-1) = 1;
    res(x, y+1) = 1;
    res(x+1, y) = 1;
end
res = res(2:end-1, 2:end-1);
return

%% imerode
function res = imerode(img)
padded = zeros(size(img)+2);
padded(2:end-1, 2:end-1) = img;

indx = find(padded);
siz = [size(padded, 1), size(padded, 2)];

res = zeros(siz);
for i=1:length(indx)
    [x, y] = ind2sub(siz, indx(i));
    neighbors = [padded(x-1, y) padded(x, y-1) padded(x, y+1) padded(x+1, y)];
    if nnz(neighbors) == length(neighbors)
        res(x, y) = 1;
    end
end
res = res(2:end-1, 2:end-1);
return