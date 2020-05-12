package be.uantwerpen.namingserver.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ServerRepository {

    HashMap<Integer, Integer> dataBase = new HashMap<>();
    HashMap<Integer, String> nodes = new HashMap<>();
    Integer highest = 0;

    public ServerRepository() {
    }

    /**
     * @return the dataBase
     */
    public HashMap<Integer, Integer> getDataBase() {
        return dataBase;
    }

    /**
     * @return the nodes
     */
    public HashMap<Integer, String> getNodes() {
        return nodes;
    }

    public void addNode(String name, String ip) throws IOException {
        addNodeToMap(name, ip);
    }

    public void removeNode(String name) throws IOException {
        removeNodeFromMap(hashfunction(name, true));
    }

    private void addNodeToMap(String name, String ip) throws IOException {
        URL url = getClass().getResource("NodeMap.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(url.getPath(), false)); // Set true for append mode
        writer.newLine(); // Add new line
        writer.write(name);
        writer.newLine();
        writer.write(ip);
        writer.close();
        readNodeMap();
        readDatabase();
    }

    private void removeNodeFromMap(Integer node) throws IOException {
        nodes.clear();
        URL url = getClass().getResource("NodeMap.txt");
        File file = new File(url.getPath());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        nodes.clear();
        ArrayList<String> nameToAdd = new ArrayList<>();
        ArrayList<String> ipToAdd = new ArrayList<>();
        while ((st = br.readLine()) != null) {
            String ip = br.readLine();
            int hash = hashfunction(st, true);
            if (hash != node) {
                nodes.put(hash, ip);
                nameToAdd.add(st);
                ipToAdd.add(ip);
            } else
                System.out.println("removed " + st);
        }
        br.close();
        int i = 0;
        BufferedWriter writer = new BufferedWriter(new FileWriter(url.getPath(), false)); // Set true for append mode
        while (i < nameToAdd.size()) {
            if (i >= 1)
                writer.newLine();
            writer.write(nameToAdd.get(i));
            writer.newLine();
            writer.write(ipToAdd.get(i));
            i++;
        }
        writer.close();
        highest = 0;
        readNodeMap();
        readDatabase();
    }

    private void readDatabase() throws IOException {
        URL url = getClass().getResource("Database.txt");
        File file = new File(url.getPath());
        BufferedReader br2 = new BufferedReader(new FileReader(file));
        String st2;
        dataBase.clear();
        while ((st2 = br2.readLine()) != null) {
            Integer tempfile = hashfunction(st2, false);
            Integer temp = tempfile - 1;
            while (nodes.get(temp) == null && temp != 0) {
                temp--;
            }
            if (temp == 0)
                dataBase.put(tempfile, highest);
            dataBase.put(tempfile, highest);
        }
        br2.close();
        System.out.println(dataBase.toString());
    }

    private void readNodeMap() throws IOException {
        URL url = getClass().getResource("NodeMap.txt");
        File file = new File(url.getPath());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        nodes.clear();
        while ((st = br.readLine()) != null) {
            if (!st.isEmpty()) {
                String ip = br.readLine();
                int hash = hashfunction(st, true);
                System.out.println("node " + st + " has a hash value of " + hash);
                nodes.put(hash, ip);
                if (hash > highest)
                    highest = hash;
            }
        }
        br.close();
    }

    private int hashfunction(String name, boolean node) {
        int hash = 0;
        int temp = 0;
        int i;
        for (i = 0; i < name.length(); i++) {
            hash = 3 * hash + name.charAt(i);
            temp = temp + name.charAt(i);
        }
        hash = hash + temp;
        if (node) {
            System.out.println("node");
        } else
            hash = hash / 53;
        return hash;
    }

}