'''
Created on May 11, 2013

@author: nvijayak
'''


#The grid
grid = [[0, 0, 0, 0, 0, 0],
        [0, 0, 1, 1, 0, 0],
        [0, 0, 1, 1, 0, 0],
        [0, 0, 1, 1, 0, 0],
        [0, 0, 0, 0, 0, 0]]

#initial position
init = [3, 4]

#goal
goal = [1,0]

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
    #closed used for marking visited blocks
    closed = [[0 for row in range(len(grid[0]))] for col in range(len(grid))]
    
    #mark initial block as visited
    closed[init[0]][init[1]] = 1
    
    expand = [[-1 for row in range(len(grid[0]))] for col in range(len(grid))]
    action = [[-1 for row in range(len(grid[0]))] for col in range(len(grid))]

    x = init[0]
    y = init[1]
    g = 0

    opened = [[g, x, y]]
    expand[x][y] = 0
    step = 1

    found = False  # flag that is set when search is complete
    resign = False # flag set if we can't find expand

    while not found and not resign:
        if len(opened) == 0:
            resign = True
        else:
            opened.sort()
            opened.reverse()
            block = opened.pop()
            x = block[1]
            y = block[2]
            g = block[0]
            
            if x == goal[0] and y == goal[1]:
                found = True
            else:
                for i in range(len(delta)):
                    x2 = x + delta[i][0]
                    y2 = y + delta[i][1]
                    if x2 >= 0 and x2 < len(grid) and y2 >=0 and y2 < len(grid[0]):
                        if closed[x2][y2] == 0 and grid[x2][y2] == 0:
                            g2 = g + cost
                            opened.append([g2, x2, y2])
                            closed[x2][y2] = 1
                            action[x2][y2] = i
                            expand[x2][y2] = step
                            step += 1
    
    print "Expand grid is..."
    for e in expand:
        print e
    
    print
    print 'Action is...'
    for a in action:
        print a
    
    print
    print 'Calculating Policy...'
    policy = [[' ' for row in range(len(grid[0]))] for col in range(len(grid))]
    
    x=goal[0]
    y=goal[1]
    policy[x][y]='*'
    #while x!=init[0] or y!=init[1]:
    while not [x,y] == init:
        x2 = x - delta[action[x][y]][0]
        y2 = y - delta[action[x][y]][1] 
        policy[x2][y2] = delta_name[action[x][y]]
        x=x2
        y=y2
        
    print
    print "Policy is..."
    for p in policy:
        print p
        
    return expand #Leave this line for grading purposes!

search()

if __name__ == '__main__':
    pass
