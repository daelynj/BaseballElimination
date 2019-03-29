/* BaseballElimination.java
   CSC 226 - Spring 2019
   Assignment 4 - Baseball Elimination Program
   
   This template includes some testing code to help verify the implementation.
   To interactively provide test inputs, run the program with
	java BaseballElimination
	
   To conveniently test the algorithm with a large input, create a text file
   containing one or more test divisions (in the format described below) and run
   the program with
	java -cp .;algs4.jar BaseballElimination file.txt (Windows)
   or
    java -cp .:algs4.jar BaseballElimination file.txt (Linux or Mac)
   where file.txt is replaced by the name of the text file.
   
   The input consists of an integer representing the number of teams in the division and then
   for each team, the team name (no whitespace), number of wins, number of losses, and a list
   of integers represnting the number of games remaining against each team (in order from the first
   team to the last). That is, the text file looks like:
   
	<number of teams in division>
	<team1_name wins losses games_vs_team1 games_vs_team2 ... games_vs_teamn>
	...
	<teamn_name wins losses games_vs_team1 games_vs_team2 ... games_vs_teamn>

	
   An input file can contain an unlimited number of divisions but all team names are unique, i.e.
   no team can be in more than one division.


   R. Little - 03/22/2019
*/

import edu.princeton.cs.algs4.*;
import java.util.*;
import java.io.File;

//Do not change the name of the BaseballElimination class
public class BaseballElimination{

    private final int num_of_teams;
    private int[] wins;
    private int[] games_left;
    private int[][] game_sched;
    private Boolean[] eliminated_teams;
    private String[] teams;
	
	// We use an ArrayList to keep track of the eliminated teams.
	public ArrayList<String> eliminated = new ArrayList<String>();

	/* BaseballElimination(s)
		Given an input stream connected to a collection of baseball division
		standings we determine for each division which teams have been eliminated 
		from the playoffs. For each team in each division we create a flow network
		and determine the maxflow in that network. If the maxflow exceeds the number
		of inter-divisional games between all other teams in the division, the current
		team is eliminated.
	*/
	public BaseballElimination(Scanner s){
        
        num_of_teams = s.nextInt();
        wins = new int[num_of_teams];
        games_left = new int[num_of_teams];
        game_sched = new int[num_of_teams][num_of_teams];
        teams = new String[num_of_teams];

        for (int i = 0; i < num_of_teams; i++) {
            teams[i] = s.next();
            wins[i] = s.nextInt();
            games_left[i] = s.nextInt();

            for (int j = 0; j < num_of_teams; j++) {
                game_sched[i][j] = s.nextInt();
            }
        }

        for (int i = 0; i < num_of_teams; i++) {
            check_elim(teams[i], i);
        }
    }

    // public void easy_elim(int team_idx) {
    //     for (int i = 0; i < num_of_teams; i++) {
    //         for (int j = 0; j < num_of_teams; j++) {
    //             if (wins[i] + games_left[i] < wins[j]) {
    //                 eliminated_teams[i] = true;
    //                 eliminated.add(teams[i]);
    //                 break;
    //             }
    //         }
    //     }
    // }

    public void check_elim(String team, int team_idx) {

        for (int i = 0; i < num_of_teams; i++) {
            if (i != team_idx && wins[team_idx] + games_left[team_idx] < wins[i]) {
                eliminated.add(teams[team_idx]);
                return;
            }
        }

        int vertices = num_of_teams + (num_of_teams - 1) * (num_of_teams - 2) / 2 + 2;
        int start = vertices - 2;
        int end = vertices - 1;

        FlowNetwork G = new FlowNetwork(vertices);
        int r = num_of_teams;
        for (int i = 0; i < num_of_teams; i++) {
            if (i == team_idx) {
                continue;
            }
            for (int j = i+1; j < num_of_teams; j++) {
                if (j == team_idx) {
                    continue;
                }
                G.addEdge(new FlowEdge(start, r, game_sched[i][j]));
                G.addEdge(new FlowEdge(r, i, game_sched[i][j]));
                G.addEdge(new FlowEdge(r, j, game_sched[i][j]));
                r++;
            }
        }

        for (int i = 0; i < num_of_teams; i++) {
            if (i != team_idx) {
                G.addEdge(new FlowEdge(i, end, wins[team_idx] + games_left[team_idx] - wins[i]));
            }
        }

        FordFulkerson max_flow = new FordFulkerson(G, start, end);

        ArrayList<String> ret = new ArrayList<String>();
        for (int v = 0; v < num_of_teams; v++) { 
            if (max_flow.inCut(v)) {
                ret.add(teams[v]);
            }
        }
        if (ret.isEmpty() == false) {
            eliminated.add(teams[team_idx]);
        }
    }
		
        /* main()
        Contains code to test the BaseballElimantion function. You may modify the
        testing code if needed, but nothing in this function will be considered
        during marking, and the testing process used for marking will not
        execute any of the code below.
        */
    public static void main(String[] args){
		Scanner s;
		if (args.length > 0){
			try{
				s = new Scanner(new File(args[0]));
			} catch(java.io.FileNotFoundException e){
				System.out.printf("Unable to open %s\n",args[0]);
				return;
			}
			System.out.printf("Reading input values from %s.\n",args[0]);
		}else{
			s = new Scanner(System.in);
			System.out.printf("Reading input values from stdin.\n");
		}
		
		BaseballElimination be = new BaseballElimination(s);		
		
		if (be.eliminated.size() == 0)
			System.out.println("No teams have been eliminated.");
		else
			System.out.println("Teams eliminated: " + be.eliminated);
	}
}