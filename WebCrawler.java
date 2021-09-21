package edu.ics211.ec;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
  
/**
 * Represents a WebCrawler.
 * @author William Liang, Help from Kanai
 *
 */
public class WebCrawler {
  
  public static HashMap<String, Node> graph = new HashMap<>();

  private static void bfs(Node n, int maxDepth) {
    Queue<Node> theQueue = new LinkedList<Node>();
    theQueue.offer(n);
    Node currentNode;

    while (!theQueue.isEmpty()) {

      currentNode = theQueue.poll();

      if (!currentNode.isVisited && currentNode.depth <= maxDepth) {

        currentNode.isVisited = true;
        graph.put(currentNode.web, currentNode);
        System.out.println("BFS Visited " + currentNode.web + " Depth: " + currentNode.depth);

        for (Node temp : currentNode.getChildren()) {
          if (temp.depth <= maxDepth) {
            theQueue.offer(temp);
          }
        }
      }
    }
  }

  private static void dfs(Node n, int maxDepth) {
    Stack<Node> theStack = new Stack<>();
    theStack.push(n);
    Node currentNode;

    while (!theStack.empty()) {

      currentNode = theStack.pop();

      if (!currentNode.isVisited && currentNode.depth <= maxDepth) {

        currentNode.isVisited = true;
        graph.put(currentNode.web, currentNode);
        System.out.println("DFS Visited " + currentNode.web + " Depth: " + currentNode.depth);

        for (Node temp : currentNode.getChildren()) {
          if (temp.depth <= maxDepth) {
            theStack.push(temp);
          }
        }
      }
    }
  }

  private static class Node {
    public ArrayList<Node> childLinks;
    private String web;
    public boolean isVisited;
    public int depth;
    
    private static boolean tryConnect(String url) {
      try {
        // Tries to connect to given url
        new URL(url).toURI();
      } catch (MalformedURLException | URISyntaxException e) {
        // Return false if unable to get a connection
        return false;
      }
      // Return true if connection is successful
      return true;
    }
    
    // Node constructor
    Node(String url, int depth) throws IOException {
      // If unable to reach website, throw IOException
      if (!validateLink(url) && !tryConnect(url)) {
        throw new IOException();
      }
      this.web = url;
      this.depth = depth;
    }
    
    private static boolean validateLink(String url) {
      try {
        // Website input argument 
        String website = url;
        // Regex to check valid URL
        // Found this at https://www.geeksforgeeks.org/check-if-an-url-is-valid-or-not-using-regular-expression/
        String regex = "((http|https)://)(www.)[a-z" 
            + "A-Z0-9@:%._\\+~#?&//=]{2,256}\\.(com|edu|mil|gov|org)"
            + "\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";
        // Compile pattern
        Pattern pattern = Pattern.compile(regex);
        // Finds match between regex string and user input website string
        Matcher websiteMatch = pattern.matcher(website);
        // If website is invalid, throw error to user
        if (!websiteMatch.matches()) {
          return false;
        }
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    
    private static String getWebsite(String url) throws IOException, InterruptedException {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .GET()
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      // Returns html page of the response given
      return response.body();
    }
    
    private static ArrayList<String> getLinks(String website) {
      // Found this at https://stackoverflow.com/questions/5120171/extract-links-from-a-web-page
      ArrayList<String> result = new ArrayList<String>();
      
      String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(website);
      while (m.find())
      {
        result.add(m.group());
      }

      return result;
      
    }
    
    public ArrayList<Node> getChildren() {
      if (childLinks != null) {
        return childLinks;
      }
      ArrayList<Node> listOfChildren = new ArrayList<Node>();
      try { 
        // Make array list of nodes and strings
        ArrayList<String> listOfLinks = getLinks(getWebsite(web));
        // Loop through list of links
        for (String link: listOfLinks) {
          try {
            // Attempt to create new node with value 'link'
            Node temp = new Node(link, depth + 1);
            // Add new node to list of children
            listOfChildren.add(temp);
          } catch (IOException e) {
            // Don't do anything
          }
        }
      } catch (IOException e) {
        // Don't do anything
      } catch (InterruptedException e) {
        // Don't do anything
      }
      childLinks = listOfChildren;
      // Return list of children array list of nodes
      return listOfChildren;
    }
    
    private static void reset() {
      // Resets the visited boolean to be able to run DFS after BFS
      for (Node node: graph.values()) {
        node.isVisited = false;
      }
    }
  
    @SuppressWarnings("unused")
    public static void main(String[] args) {
      try {
        Node node = new Node(args[0], 0);
        bfs(node, Integer.parseInt(args[1]));
        reset();
        dfs(node, Integer.parseInt(args[1]));

      } catch (IOException e) {
        System.out.println("Error: invalid web address");
      }
    }
  }
}
