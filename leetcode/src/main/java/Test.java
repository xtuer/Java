import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Integer> path = new ArrayList<>();
        List<Integer> selection = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        // permutation(path, selection, 3);
        // combination(path, selection, 0, 3);

        System.out.println(Test.class.getClassLoader());

        MyClassLoader cl = new MyClassLoader();
        System.out.println(cl.getParent());
    }

    /**
     * 求排列。
     */
    public static void permutation(List<Integer> path, List<Integer> selection, final int count) {
        if (path.size() == count) {
            System.out.println(path);
            return;
        }

        for (int i = 0; i < selection.size(); i++) {
            path.add(selection.get(i));

            List<Integer> nextSelection = new LinkedList<>(selection);
            Integer e = selection.get(i);
            nextSelection.remove(e);
            permutation(path, nextSelection, count);

            path.remove(path.size() - 1);
        }
    }

    /**
     * 求组合。
     */
    public static void combination(List<Integer> path, List<Integer> selection, final int startPos, final int count) {
        if (path.size() == count) {
            System.out.println(path);
            return;
        }

        // startPos 控制不重复
        for (int i = startPos; i < selection.size(); i++) {
            path.add(selection.get(i));
            combination(path, selection, i+1, count);
            path.remove(path.size() - 1);
        }
    }
}

class MyClassLoader extends ClassLoader {

}
