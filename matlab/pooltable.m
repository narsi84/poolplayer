function pooltable
clc;

global fov table BALL_SIZE accel;

BALL_SIZE = 5;
accel = 10;

fov.width = 350;
fov.height = 200;

table.width = 300;
table.height = 150;
table.x = 25;
table.y = 25;
table.pocket_r = 10;
table.NUM_POCKETS = 6;

w = table.width/2;
h = table.height/2;
r = table.pocket_r;

table.pockets = [ [-r -r]; ...       %Bottom left
    [-r 2*h+r]; ...    %Top left
    [w -r]; ...        %Center bottom
    [w 2*h+r];         %Center top
    [2*w+r -r]; ...    %Bottom right
    [2*w+r 2*h+r]; ... %Top right
    ];

table.pockets = table.pockets + repmat([table.x table.y], 6, 1);

plotTable();


cue = [rand*150+50 rand*120+40];
target = [rand*150+50 rand*120+40];

plotCircle(cue(1), cue(2), BALL_SIZE, 'y');
plotCircle(target(1), target(2), BALL_SIZE, 'r');

otherballs = [];
% plotCircle(otherballs(1), otherballs(2), BALL_SIZE, 'b');

for i=1:5
    randball = [rand*150+50 rand*120+40];
    plotCircle(randball(1), randball(2), BALL_SIZE, 'b');   
    otherballs = [otherballs; randball];
end

for i=1:table.NUM_POCKETS
    pocket = table.pockets(i, :);
    ghost = findGhost(target, pocket);

%     plotCircle(ghost(1), ghost(2), BALL_SIZE, '--m');
%     plotCircle(pocket(1), pocket(2), table.pocket_r, '--g');

    if isShotPossible(cue, ghost, pocket)
        
        pathClear = true;
        for j=1:size(otherballs, 1)
            if isBallInPath(cue, ghost, otherballs(j, :)) 
                pathClear = false;
                break;
            end
        end
        
        if ~pathClear
            continue;
        end
        
        for j=1:size(otherballs, 1)
            if isBallInPath(target, pocket, otherballs(j, :)) 
                pathClear = false;
                break;
            end
        end
        
        if pathClear
            plotCircle(ghost(1), ghost(2), BALL_SIZE, 'm');
            plotCircle(pocket(1), pocket(2), table.pocket_r, 'g');
            
            u_c = findInitVel(cue, ghost, target, pocket);
            text(pocket(1), pocket(2), num2str(u_c));            
        end
    end
end

% pc = findBestPocket(target);

return

%%Find initial velocity of cue to pot target into pocket
function u_c = findInitVel(cue, ghost, target, pocket)
global accel;

theta_gc = atan2(ghost(2) - cue(2), ghost(1) - cue(1));
theta_gt = atan2(target(2) - ghost(2), target(1) - ghost(1));
theta_ct = theta_gc - theta_gt;
s = norm(target - pocket);
d = norm(cue - ghost);

u_c = sqrt( 2*accel*s/(cos(theta_ct)*cos(theta_ct)) + 2*accel*d );

return
%% Is shot possible
%The angle subtended at ghost by cue-ghost and ghost-pocket vectors must be
%acute
function ret = isShotPossible(cue, ghost, pocket)
 
ret = true;
cg = norm(cue - ghost);
gp = norm(ghost - pocket);
cp = norm(cue - pocket);

%Law of cosines
theta = acos( (gp*cp + cg*cg - cp*cp)/(2*gp*cg));
if theta<pi/2
    ret = false;
end
return

%% Find ghost ball
function ghost = findGhost(ball, pocket)
global BALL_SIZE table;
theta = atan2(pocket(2)-ball(2), pocket(1)-ball(1));
ghost = [ball(1)-2*BALL_SIZE*cos(theta) ball(2)-2*BALL_SIZE*sin(theta)];

return

%% Is any ball in path to object
function ret = isBallInPath(src_t, dest_t, ball)
global BALL_SIZE

if norm(src_t - ball) < 2*BALL_SIZE || norm(dest_t - ball) < 2*BALL_SIZE
    ret = true;
    return;
end

error = 2;

src = src_t - ball;
dest = dest_t - ball;

theta = pi/2 - atan2(dest(2) - src(2), dest(1) - src(1));

%Center
dr = norm(dest - src);  
D = src(1)*dest(2) - dest(1)*src(2);

%Right line
src_r = [src(1) + BALL_SIZE*cos(theta) src(2) - BALL_SIZE*sin(theta)];
dest_r = [dest(1) + BALL_SIZE*cos(theta) dest(2) - BALL_SIZE*sin(theta)];
D_r = src_r(1)*dest_r(2) - dest_r(1)*src_r(2);

%Left line
src_l = [src(1) - BALL_SIZE*cos(theta) src(2) + BALL_SIZE*sin(theta)];
dest_l = [dest(1) - BALL_SIZE*cos(theta) dest(2) + BALL_SIZE*sin(theta)];
D_l = src_l(1)*dest_l(2) - dest_l(1)*src_l(2);

plot([src(1) dest(1)]+ball(1), [src(2) dest(2)]+ball(2), '--m');
plot([src_r(1) dest_r(1)]+ball(1), [src_r(2) dest_r(2)]+ball(2), '--m');
plot([src_l(1) dest_l(1)]+ball(1), [src_l(2) dest_l(2)]+ball(2), '--m');

delta_c = BALL_SIZE*BALL_SIZE*dr*dr - D*D;
delta_r = BALL_SIZE*BALL_SIZE*dr*dr - D_r*D_r;
delta_l = BALL_SIZE*BALL_SIZE*dr*dr - D_l*D_l;

%Ball cannot be in path is src-dest dist > src-ball. Accout for radius of
%ball and source
if norm(src_t - dest_t) + 2*BALL_SIZE - error < norm(src_t - ball)
    ret = false;
    return
end

if delta_c > 0 || delta_l > 0 || delta_r > 0
    %If both dest and src are on the same size of ball, there is no
    %overlap. The angle subtended at ball by src-ball and dest-ball will be
    %acute. The isShotPossible() calculates this, so reuse with diff args
    if ~isShotPossible(src_t, ball, dest_t)
        ret = false;
    else
        ret = true;
    end        
else
    ret = false;
end

return

%% Plot table
function plotTable()

global fov table
clf;
rectangle('Position', [table.x table.y table.width table.height], 'LineWidth', 2);
xlim([0 fov.width]);
ylim([0 fov.height]);

hold on;

for i=1:size(table.pockets, 1)
    pc = table.pockets(i, :);
    plotCircle(pc(1), pc(2), table.pocket_r, 'k');
end
return

%% Plot circle
function plotCircle(x, y, r, color)

xi = x-r:x+r;
yi = y + sqrt(r*r - (xi - x).*(xi - x));
yi = [yi y - sqrt(r*r - (xi - x).*(xi - x))];

plot([xi fliplr(xi)], yi, color, 'Linewidth', 2);
return