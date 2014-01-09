fname = '../OpenCVTest/images/balls3.jpeg';
img = double(imread(fname));

maxcolor = max(img, [], 3);
normimg  = cat(3, cat(3, (img(:, :, 1)./maxcolor), img(:, :, 2)./maxcolor), img(:, :, 3)./maxcolor);

g = zeros(size(img, 1), size(img,2));
ang = zeros(size(img, 1), size(img,2));
for i=2:size(img, 1)-1
    for j = 2:size(img, 2) - 1
        gx =  -normimg(i-1, j-1, :) - 2*normimg(i, j-1, :) - normimg(i+1, j-1, :) ...
               +normimg(i+1, j+1, :) + 2*normimg(i, j+1, :) + normimg(i+1, j+1, :);
        gx = norm(gx(:));
        
        gy =  -normimg(i-1, j-1, :) - 2*normimg(i-1, j, :) - normimg(i-1, j+1, :) ...
               +normimg(i+1, j-1, :) + 2*normimg(i+1, j, :) + normimg(i+1, j+1, :);
        gy = norm(gy(:));        
        
        g(i, j) = sqrt(gx*gx + gy*gy);
        ang(i, j) = atan2(gy, gx);
    end
end

imagesc(g);
imwrite(g>1.5, '../OpenCVTest/images/balls3_edges.jpeg');