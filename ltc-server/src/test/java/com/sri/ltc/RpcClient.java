/**
 ************************ 80 columns *******************************************
 * RpcClient
 *
 * Created on May 17, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc;

import com.sri.ltc.filter.Author;
import com.sri.ltc.git.Commit;
import com.sri.ltc.server.HelloLTC;
import com.sri.ltc.server.LTCserverInterface;
import org.apache.xmlrpc.XmlRpcException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author linda
 */
public class RpcClient {

    static LTCserverInterface server;

    private static void printHelp() {
        System.out.println("\nFollowing commands are available: (Optional parameters in square brackets)");
        System.out.println(" i - init_session(String path)");
        System.out.println(" c - close_session(int ID)");
        System.out.println(" g - get_changes(int ID)\n");
        System.out.println(" s - get_show(String key) and set_show(String key, bool value)");
        System.out.println(" r - reset_show");
        System.out.println(" l - get_show_items\n");
        System.out.println(" a - get_authors(int sessionID)");
        System.out.println(" p - get_commits(int sessionID)");
        System.out.println(" t - get_self(int sessionID) and set_self(int sessionID, String name, String email)");
        System.out.println(" u - reset_self(int sessionID)\n");
        System.out.println(" b - get_limited_authors(int sessionID)");
        System.out.println(" d - set_limited_authors(int sessionID, List indices)");
        System.out.println(" k - reset_limited_authors(int sessionID)\n");
        System.out.println(" m - get_limited_date(int sessionID) and set_limited_date(int sessionID, String date)");
        System.out.println(" o - reset_limited_date(int sessionID)\n");
        System.out.println(" n - get_limited_rev(int sessionID) and set_limited_rev(int sessionID, String rev)");
        System.out.println(" j - reset_limited_rev(int sessionID)\n");
        System.out.println(" e - get_color(String name, String email) and set_color(String name, String email, String color)");
        System.out.println(" f - reset_all_colors()\n");
        System.out.println(" z - hello()\n");
        System.out.println(" h - HELP");
        System.out.println(" q - QUIT");
        System.out.println();
    }

    private static void printAuthorMap(List<Object[]> authors) {
        if (authors != null)
            for (Object[] a : authors) {
                System.out.println("  "+ Author.fromList(a)+" -> "+a[2]);
            }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        // obtain server instance:
        HelloLTC.Client client = new HelloLTC.Client(new URL("http://localhost:"+LTCserverInterface.PORT+"/xmlrpc"));
        server = (LTCserverInterface) client.GetProxy(LTCserverInterface.class);

        // get STDIN:
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        if (in == null) {
            System.err.println("No access to input -- exiting.");
            System.exit(1);
        }

        // loop through input until CTRL-C:
        printHelp();
        System.out.println("Type XML-RPC requests after prompt:");
        System.out.print(">> ");
        System.out.flush();
        Object[] authorAsList;
        Map map;
        for (String line;
             (line = in.readLine()) != null;
             System.out.print(">> "), System.out.flush()) {
            try {
                // parse line
                line = line.trim();
                String[] tokens = line.split("\\s"); // split at white space
                if (line.length() > 0)
                    switch (line.toUpperCase().charAt(0)) {
                        case 'I':
                            System.out.println("Session ID = "+server.init_session(tokens[1]));
                            break;
                        case 'C':
                            map = server.close_session(Integer.parseInt(tokens[1]), "", Collections.emptyList(), 0);
                            String text = (String) map.get(LTCserverInterface.KEY_TEXT);
                            if (text != null)
                                text = text.substring(0,text.length()>20?20:text.length());
                            System.out.println("Session text = "+text);
                            break;
                        case 'G':
                            map = server.get_changes(Integer.parseInt(tokens[1]), false, "", Collections.emptyList(), 0);
                            System.out.println("text = "+map.get(LTCserverInterface.KEY_TEXT));
                            System.out.println("styles = ");
                            Object[] list = (Object[]) map.get(LTCserverInterface.KEY_STYLES);
                            if (list != null)
                                for (int i=0; i < list.length; i++) {
                                    Object[] element = (Object[]) list[i];
                                    System.out.print("  ");
                                    for (int j=0; j < element.length; j++)
                                        System.out.print(element[j]+" ");
                                    System.out.println();
                                }
                            System.out.println("authors by key = ");
                            for (Map.Entry<String,Object[]> e :
                                    ((Map<String,Object[]>) map.get(LTCserverInterface.KEY_AUTHORS)).entrySet())
                                System.out.println("  "+e.getKey()+" -> "+ Author.fromList(e.getValue())+" ("+e.getValue()[2]+")");
                            System.out.println("sha1 keys = ");
                            for (Object sha1 : (Object[]) map.get(LTCserverInterface.KEY_SHA1))
                                System.out.println("  "+sha1.toString());
                            break;
                        case 'S':
                            if (tokens.length > 2)
                                server.set_show(tokens[1], Boolean.parseBoolean(tokens[2]));
                            else
                                System.out.println("show status = "+server.get_show(tokens[1]));
                            break;
                        case 'R':
                            server.reset_show();
                            break;
                        case 'L':
                            System.out.println("Show items are:");
                            for (Object name : server.get_show_items())
                                System.out.print(name+" ");
                            System.out.println();
                            break;
                        case 'A':
                            System.out.println("Authors are:");
                            printAuthorMap((List<Object[]>) server.get_authors(Integer.parseInt(tokens[1])));
                            break;
                        case 'B':
                            System.out.println("Limited authors are:");
                            printAuthorMap((List<Object[]>) server.get_limited_authors(Integer.parseInt(tokens[1])));
                            break;
                        case 'D':
                            List<String[]> authors = new ArrayList<String[]>();
                            for (int i=2; i < tokens.length-1; i+=2)
                                authors.add(new String[] {tokens[i], tokens[i+1]});
                            System.out.println("Limited authors are now:");
                            printAuthorMap((List<Object[]>) server.set_limited_authors(Integer.parseInt(tokens[1]), authors));
                            break;
                        case 'K':
                            server.reset_limited_authors(Integer.parseInt(tokens[1]));
                            break;
                        case 'M':
                            if (tokens.length > 2) {
                                StringBuilder date = new StringBuilder();
                                for (int i = 2; i < tokens.length-1; i++)
                                    date.append(tokens[i]+" ");
                                System.out.println("Limit date is now: "+server.set_limited_date(Integer.parseInt(tokens[1]), date.toString().trim()));
                            } else
                                System.out.println("Limit date is: "+server.get_limited_date(Integer.parseInt(tokens[1])));
                            break;
                        case 'O':
                            server.reset_limited_date(Integer.parseInt(tokens[1]));
                            break;
                        case 'P':
                            System.out.println("Commits:");
                            for (Object[] object : (List<Object[]>) server.get_commits(Integer.parseInt(tokens[1]))) {
                                try {
                                    // TODO: replace this with individual prints, or 
                                    System.out.println("  "+ Commit.fromArray(object));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case 'T':
                            if (tokens.length > 2) {
                                StringBuilder name = new StringBuilder();
                                for (int i = 2; i < tokens.length-1; i++)
                                    name.append(tokens[i]+" ");
                                authorAsList = server.set_self(Integer.parseInt(tokens[1]),
                                        name.toString().trim(),
                                        tokens[tokens.length-1]);
                            } else
                                authorAsList = server.get_self(Integer.parseInt(tokens[1]));
                            if (authorAsList == null || authorAsList.length < 3)
                                System.out.println("Couldn't get self");
                            else
                                System.out.println("Self = "+ Author.fromList(authorAsList)+" with color "+authorAsList[2]);
                            break;
                        case 'U':
                            authorAsList = server.reset_self(Integer.parseInt(tokens[1]));
                            if (authorAsList == null || authorAsList.length < 2)
                                System.out.println("Couldn't get self");
                            else
                                System.out.println("Self = "+ Author.fromList(authorAsList));
                            break;
                        case 'N':
                            if (tokens.length > 2) {
                                System.out.println("Limit rev is now: "+server.set_limited_rev(Integer.parseInt(tokens[1]), tokens[2]));
                            } else
                                System.out.println("Limit rev is: "+server.get_limited_rev(Integer.parseInt(tokens[1])));
                            break;
                        case 'J':
                            server.reset_limited_rev(Integer.parseInt(tokens[1]));
                            break;
                        case 'E':
                            if (tokens.length > 3) {
                                server.set_color(tokens[1], tokens[2], tokens[3]);
                            } else
                                System.out.println("Color is: "+server.get_color(tokens[1], tokens[2]));
                            break;
                        case 'F':
                            server.reset_all_colors();
                            break;
                        case 'Z':
                            System.out.println("The answer is: "+server.hello());
                            break;
                        case 'H':
                            printHelp();
                            break;
                        case 'Q':
                            System.out.println("Good bye.");
                            System.exit(0);
                        default:
                            System.out.println("Didn't understand command "+line);
                    }
            } catch (XmlRpcException e) {
                System.err.println(" *** ERROR="+e.code);
                e.printStackTrace();
            }
        }
    }
}
