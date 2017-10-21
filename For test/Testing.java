import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.Map.Entry;

public class Testing {
    public static void main(String[] args) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(1000, 0);
        map.put(1001, 3);
        map.put(1002, 1);

        System.out.println("unsorted map: " + map);


    List<Entry<Integer, Integer>> list = new LinkedList<>(map.entrySet());
    Collections.sort(list, new Comparator<Object>() {
        public int compare(Object o1, Object o2) {
            return -((Comparable<Integer>) ((Map.Entry<Integer, Integer>) (o1)).getValue()).compareTo(((Map.Entry<Integer, Integer>) (o2)).getValue());
        }
    });

    Map<Integer, Integer> sorted_map = new LinkedHashMap<>();
    for (Iterator<Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
        Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();
        sorted_map.put(entry.getKey(), entry.getValue());
    }

        System.out.println("results: " + sorted_map);

System.out.println("size : " + sorted_map.size() );
System.out.println(sorted_map.values().toArray()[0]);
System.out.println(sorted_map.keySet().toArray()[0]);
System.out.println(sorted_map.values().toArray()[1]);
System.out.println(sorted_map.keySet().toArray()[1]);
    }
}

