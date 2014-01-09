a = 5;
cue = [50 100];
ghost = [110 100];
target = [125 120];
pocket = [150 100];

theta_gc = atan2(ghost(2) - cue(2), ghost(1) - cue(1));
theta_gt = atan2(target(2) - ghost(2), target(1) - ghost(1));
theta_ct = theta_gc - theta_gt;
s = norm(target - pocket);
d = norm(cue - ghost);

u_c = sqrt( 2*a*s/(cos(theta_ct)*cos(theta_ct)) + 2*a*d )