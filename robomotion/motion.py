'''
Created on Mar 14, 2012

@author: nvijayak
'''


#The grid
grid = [[0, 0, 0, 0, 0, 0],
        [0, 0, 0, 1, 0, 0],
        [0, 1, 1, 1, 0, 0],
        [0, 0, 0, 1, 0, 0],
        [0, 0, 0, 0, 0, 0]]

#initial position
init = [0, 0]

#goal
goal = [4,5]

#delta
delta = [[-1, 0 ], # go up
         [ 0, -1], # go left
         [ 1, 0 ], # go down
         [ 0, 1 ]] # go right

#direction name
delta_name = ['^', '<', 'v', '>']


cost = 1

#search shortest path
def search():
    #used for marking visited blocks
    visited = [[0 for row in range(len(grid[0]))] for col in range(len(grid))]
    
    #mark initial block as visited
    visited[init[0]][init[1]] = 1
    
    #DS to rem. the action to get to a particular block
    action = [[-1 for row in range(len(grid[0]))] for col in range(len(grid))]

    x = init[0]
    y = init[1]
    g = 0

    #opened is DS which has the g-value (cost)
    #x and y location
    opened = [[g, x, y]]

    found = False  # flag that is set when search is complete
    resign = False # flag set if we can't find goal

    while not found and not resign:
        if len(opened) == 0:
            resign = True
        else:
            #get the block in the maze
            #which has the min g-value
            #Note: sort will use the first value
            #in [g, x, y] to sort the list ie the
            #g-value
            opened.sort() 
            opened.reverse()
            block = opened.pop()
            x = block[1]
            y = block[2]
            g = block[0]
            
            if x == goal[0] and y == goal[1]:
                #if goal is found set the 
                #found flag
                found = True
            else:
                #for each action (N< S, W, E)
                #check if it is valid and if it is
                #then add it to the opened list
                
                for i in range(len(delta)):
                    #get the new x and y location
                    x2 = x + delta[i][0]
                    y2 = y + delta[i][1]
                    if x2 >= 0 and x2 < len(grid) and y2 >=0 and y2 < len(grid[0]):
                        #if new x, y are valid
                        if visited[x2][y2] == 0 and grid[x2][y2] == 0:
                            #if it is already visited and if it is not
                            #blocked
                            #calc. cost
                            g2 = g + cost
                            #add it ti the opened list
                            opened.append([g2, x2, y2])
                            #mark block as visited
                            visited[x2][y2] = 1
                            #remember the action 
                            action[x2][y2] = i    

    
    #only if goal is found 
    #trace back the path
    if found==False:
        print "Goal unreachable"
        return
    
        
    print 'Calculating policy...'
    policy = [[' ' for row in range(len(grid[0]))] for col in range(len(grid))]
    x=goal[0]
    y=goal[1]
    policy[x][y]='*'

    #trace back the path from 
    #goal to initial position
    while (not [x,y] == init):
        #reverse the action that is 
        #done to get to the prev. block
        x2 = x - delta[action[x][y]][0]
        y2 = y - delta[action[x][y]][1]
        #update the policy for new  x and y 
        policy[x2][y2] = delta_name[action[x][y]]
        #update x, y and repeat the loop 
        #until initial position is reached
        x=x2
        y=y2
        
    print
    print "Policy is..."
    for p in policy:
        print p
        

search()

if __name__ == '__main__':
    pass