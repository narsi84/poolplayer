   
# PRAIS - Pool playing Robotic Autonomous Intelligent System
 
PRAIS is an autonomous and artificially intelligent robotic system capable of playing 8-ball pool.

It consists of a tabletop pool board, LEGO Mindstroms based mobile robotic unit, camera and a computer which runs the PRAIS software

The PRAIS software performs the following functions:
- Identify various types of balls on the board (Stripes, Solids, Cue and Black)
- Identify the location of the bot
- Calculate all possible valid shots and choose the best
- Compute the position for the bot to play the shot and plan the optimal path
- Command the bot to make the shot

At start, the system is set up to play either solids or stripes.

The camera connected to the computer provides an aerial view of the pool board and the bot. PRAIS software uses OpenCV libraries to process the camera feed. First, all circular objects within the pool board are identified as balls. These are then classified as Solids, Stripes, Cue or Black based on pixel RGB and intensity values.

The bot's position and orientation are determined using certain fiduciary markers.

Next, all playable shots on the board, i.e., shots where a ball can be pocketed, are identified. The complexity of each shot is computed using rigid body dynamics and the simplest one is chosen.

The position for the bot to play this shot is determined and the shortest path to get to this position is calculated using AI algorithms.

Navigation instructions are then sent to the bot via Bluetooth. Once the bot is in position, the cue stick mounted on it is released. After the shot is made, the cue stick is retracted and the system is ready for the next shot.

[![PRAIS](https://img.youtube.com/vi/e4jms5UXohw/0.jpg)](https://www.youtube.com/watch?v=e4jms5UXohw)
