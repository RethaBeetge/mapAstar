package map.walker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

/**
 * Hello world!
 *
 */
public class App 
{
	private Stack<Node> path = new Stack<>();
    private App.Node start = null;
    private App.Node end = null;
	
    public static void main( String[] args )
    {
        App app = new App();
        app.walkMap();
    }
    
    private void walkMap() {
    	ArrayList<ArrayList<App.Node>> map = null;
    	ArrayList<App.Node> openSet = new ArrayList<>();
        App.Node curr = null;
         
        map = readMap("mapInput.txt");
        curr = start;
        
        // Create collection of unprocessed nodes
        for (ArrayList<App.Node>row:map) {
        	for (App.Node node:row) {
        		openSet.add(node);
        	}
        }
        
        path.push(start);
        while (!openSet.isEmpty()) {
        	System.out.print(curr.getContent());
        	if (curr.getForward() == null && curr.getCol() + 1 < map.get(curr.getRow()).size()) {
        		curr.setForward(map.get(curr.getRow()).get(curr.getCol()+1));
        	}
        	if (curr.getLeft() == null && curr.getRow() - 1 >= 0) {
        		curr.setLeft(map.get(curr.getRow()-1).get(curr.getCol()));
        	}
        	if (curr.getRight() == null && curr.getRow() + 1 < map.size()) {
        		curr.setRight(map.get(curr.getRow()+1).get(curr.getCol()));
        	}
        	App.Node next = findNext(curr, end, map);
        	if (next != null && 'E' == next.getContent()) {
        		curr.setContent('*');
        		break;
        	}
        	if (next != null && openSet.contains(next)) {
        		next.setBack(curr);
        		if ('S' == curr.getContent()) {
        			next.setBack(curr);
        			next.setStepsFromStart(1);
        			curr = next;
        			continue;
        		}
        		if ('.' == curr.getContent() || '*' == curr.getContent()) {
        			curr.setContent('*');
        			path.push(curr);
        			openSet.remove(curr);
        			next.setBack(curr);
        			next.setStepsFromStart(curr.getStepsFromStart()+1);
        			curr = next;
        		} else if ("E".equalsIgnoreCase(Character.toString(next.getContent()))) {
        			curr.setContent('*');
        			break; // Reached end, stop loop
        		}
        	} else {
        		next = findNext(curr, end, map);
        		if (next == null && curr.getContent() != 'S') {
        			curr.setContent('"');
        			if (curr.getBack().getMoveOptions() != null && !curr.getBack().getMoveOptions().isEmpty()) {
        				curr.getBack().getMoveOptions().remove(curr);
        			}
        			curr = curr.getBack();
        		}
        	}
	    	
        }
        createResult(map);
    }

    private void createResult(ArrayList<ArrayList<App.Node>> map) {
    	String fileName = "C:\\workspace\\mapWalker\\src\\map\\walker\\mapOutput.txt";
    	
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
    		StringBuilder sb = new StringBuilder();
    		for (ArrayList<Node> list:map) {
    			for (Node node:list) {
    				sb.append(node.getContent());
    			}
    			writer.write(sb.toString());
    			writer.newLine();
    			sb.delete(0, sb.length());
    		}
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    private ArrayList<ArrayList<App.Node>> readMap(String fileName) {
    	ArrayList<ArrayList<App.Node>> content = null;
    	File file = Paths.get("C:\\workspace\\mapWalker\\src\\map\\walker\\mapInput.txt").toFile();
    	
    	try (FileReader fr = new FileReader(file);
    		 BufferedReader sc = new BufferedReader(fr)) {
    		content = new ArrayList<ArrayList<App.Node>>();
    		int row = 0;
    		String line = sc.readLine();
    		while (line != null) {
    			ArrayList<App.Node> nodes = buildRow(line, row);
    			content.add(nodes);
    			if (line.indexOf('S') >= 0) {
    				setStart(nodes.get(line.indexOf('S')));
    			}
    			if (line.indexOf('E') >= 0) {
    				setEnd(nodes.get(line.indexOf('E')));
    			}
    			line = sc.readLine();
    			row++;
    		}
    		
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     	return content;
    }
    
    private ArrayList<App.Node> buildRow(String line, int rowCounter) {
    	ArrayList<App.Node> row = new ArrayList<>();
    	char[] chars = line.toCharArray();
    	
    	for(int i = 0; i < chars.length; i++) {
    		App.Node node = new App.Node(rowCounter, i, chars[i]);
    		row.add(node);
    	}
    	
    	return row;
    }
    
    private App.Node findNext(App.Node curr, App.Node end, ArrayList<ArrayList<App.Node>> map) {
    	ArrayList<Node> possibleMoves = curr.getMoveOptions();
    	App.Node next = null;
    	
    	if (possibleMoves.isEmpty()) {
    		return next;
    	}
    	int movesRem = Math.abs(curr.getRow() - end.getRow()) + Math.abs(curr.getCol() - end.getCol()) + 1;
    	for (App.Node node:possibleMoves) {
    		if (node.getContent() == '"') {
    			possibleMoves.remove(node);
    			continue;
    		}
    		int movesToE = Math.abs(node.getRow() - end.getRow())
    				+ Math.abs(node.getCol() - end.getCol());
    		if (movesToE <= movesRem) {
    			next = node;
    			movesRem = movesToE;
    			continue;
    		}
    	}
    	
    	return next;
    }
    
    private App.Node getStart() {
    	return start;
    }
    private void setStart(App.Node node) {
    	start = node;
    }
    
    private Node getEnd() {
    	return end;
    }
    private void setEnd(App.Node node) {
    	end = node;
    }
    
    private static class Node {
    	private int row;
    	private int col;
    	private char content;
    	private Node forward;
    	private Node left;
    	private Node right;
    	private Node back;
    	private int stepsFromStart = 0;
    	private int stepsToFinish = 0;
    	private ArrayList<App.Node> possibleMoves = null;
    	
    	public Node(int row, int col, char content) {
    		this.row = row;
    		this.col = col;
    		this.content = content;
    	}
    	
    	public void setRow(int pos) {
    		this.row = pos;
    	}
    	public int getRow() {
    		return this.row;
    	}
    	
    	public void setCol(int pos) {
    		this.col = pos;
    	}
    	public int getCol() {
    		return this.col;
    	}
    	
    	public char getContent() {
			return content;
		}

		public void setContent(char content) {
			this.content = content;
		}

		public Node getForward() {
			return forward;
		}

		public void setForward(Node forward) {
			this.forward = forward;
		}

		public Node getLeft() {
			return left;
		}

		public void setLeft(Node left) {
			this.left = left;
		}

		public Node getRight() {
			return right;
		}

		public void setRight(Node right) {
			this.right = right;
		}

		public Node getBack() {
			return back;
		}

		public void setBack(Node back) {
			this.back = back;
		}

		public int getStepsFromStart() {
			return stepsFromStart;
		}

		public void setStepsFromStart(int stepsFromStart) {
			this.stepsFromStart = stepsFromStart;
		}

		public int getStepsToFinish() {
			return stepsToFinish;
		}

		public void setStepsToFinish(int stepsToFinish) {
			this.stepsToFinish = stepsToFinish;
		}
		
		public ArrayList<App.Node> getMoveOptions() {
			if (this.possibleMoves == null) {
				possibleMoves = new ArrayList<App.Node>();
				if (getForward() != null && (getForward().getContent() == '.' || getForward().getContent() == 'E')) {
					possibleMoves.add(getForward());
				}
				if (getLeft() != null && (getLeft().getContent() == '.' || getForward().getContent() == 'E')) {
					possibleMoves.add(getLeft());
				}
				if (getRight() != null && (getRight().getContent() == '.' || getForward().getContent() == 'E')) {
					possibleMoves.add(getRight());
				}
			}
			return possibleMoves;
		}

		public boolean equals(Object o) {
    		if (!(o instanceof Node)) {
    			return false;
    		}
    		if (this.getRow() == ((Node)o).getRow() &&
    			this.getCol() == ((Node)o).getCol()) {
    			return true;
    		}
    		
    		return false;
    	}
    }
}
