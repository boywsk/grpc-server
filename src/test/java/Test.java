import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangshikai on 2016/10/10.
 */
public class Test {
    public static void main(String[] args) {
//        ConcurrentSkipListSet<Boolean> set = new ConcurrentSkipListSet<>();
////        set.add(true);
//        if(!set.contains(true)){
//            set.add(true);
//            System.out.println("true");
//            if(set.contains(true)){
//                System.out.println("contains");
//            }
//        }else{
//            System.out.println("false");
//        }
//        ConcurrentSkipListSet<String> IPLIST = new ConcurrentSkipListSet<>();
//        IPLIST.add("1");
//        IPLIST.add("2");
//        IPLIST.add("3");
//        IPLIST.add("4");
//        System.out.println(IPLIST.add("5"));
//        System.out.println(IPLIST.add("5"));
        ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
        map.put("1","1");
        map.put("2","2");
        map.put("3","3");
        map.put("4","4");
        map.put("5","5");
        Object[] arr = map.entrySet().toArray();
        Map.Entry<String,String> entry = (Map.Entry<String,String>)arr[2];
        System.out.println(entry.getKey());


    }
}
