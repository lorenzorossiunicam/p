package it.unicam.pros.pplg.util.z3;

//import com.microsoft.z3.ArithExpr;
//import com.microsoft.z3.BoolExpr;
//import com.microsoft.z3.Context;
//import com.microsoft.z3.Solver;
//import it.unicam.pros.guidedsimulator.util.BinaryNode;
//import it.unicam.pros.guidedsimulator.util.BinaryTree;
//
//import java.util.*;

public class ScriptToZ3 {
//
//    private static final String comp = "==|>=|<=|===|!=|>|<";
//    private static final String spaces = "\\s+";
//    private static final String ops = ".+.|.*.|.-.|./.";
//    private static final String opsAndNums = "[^a-zA-Z]+";
//    private static final char OPEN = '(', CLOSE = ')' ;
//    private static final char PLUS = '+', MINUS = '-', MUL = '*', DIV = '/';
//
//    public static BoolExpr parseBoolExpr(String script){
//
//        String[] prep = script.split(spaces);
//        String processed = "";
//        for (int i = 0; i< prep.length; i++){
//            processed += prep[i];
//        }
//        System.out.println("NO SPACES-> "+processed);
//        String[] tmp = (new String(processed)).split(comp);
//
//        if(tmp.length!=2) return null; //not a good expression
//        for (int i = 0; i< tmp.length; i++){
//            System.out.println("PARTS-> "+tmp[i]);
//        }
//        String operator = processed.substring(tmp[0].length(),processed.length()-tmp[1].length());
//        System.out.println("OPERATORE-> "+operator);
//
//        final Context context = new Context();
//
//        List<String> variables = getVars(tmp);
//        final List<ArithExpr> z3vars = new ArrayList<ArithExpr>(variables.size());
//        for(String v : variables){
//            System.out.println("VARIABLE-> "+v);
//            z3vars.add(context.mkIntConst(v));
//        }
//
//        Map<Integer,Integer> leftBlocks = getBlocks(tmp[0]);
//        Map<Integer,Integer> rightBlocks = getBlocks(tmp[1]);
//        System.out.println(leftBlocks);
//        System.out.println(rightBlocks);
//
//        BinaryTree<String> leftTree = createTree(new BinaryTree<String>(),tmp[0], leftBlocks);
//        BinaryTree<String> rightTree = createTree(new BinaryTree<String>(), tmp[1], rightBlocks);
//
//
//        final Solver solver = context.mkSimpleSolver();
//
//        final ArithExpr a = context.mkIntConst("a");
//        final ArithExpr b = context.mkIntConst("b");
//        final BoolExpr aeq1 = context.mkEq(a, context.mkInt(1));
//        final BoolExpr aplusbeq10 = context.mkEq(context.mkAdd(a,b),context.mkInt(10));
//        final BoolExpr expr = context.mkAnd(aeq1,aplusbeq10);
//        return null;
//    }
//
//    private static BinaryTree<String> createTree(BinaryTree<String> tree, String str, Map<Integer, Integer> blocks) {
//
//        int rootPosition = findRoot(str, blocks);
//        System.out.println(rootPosition);
//
//        if(tree.getRoot()==null) {
//            if(rootPosition == -1){
//                tree.setRoot(new BinaryNode(str, null));
//                return tree;
//            }
//            tree.setRoot(new BinaryNode(str.charAt(rootPosition), null));
//        }
//
//       // tree.getRoot().setLeft(createTree());
//
//        return tree;
//    }
//
//    private static int findRoot(String str, Map<Integer, Integer> blocks) {
//        int alternative = -1; boolean foundAlt = false;
//        for(int i = str.length()-1; i>0; i--){
//            for (Map.Entry<Integer,Integer> b : blocks.entrySet()){
//                if (i ==b.getValue().intValue()) {
//                    i = b.getKey().intValue();
//                }
//            }
//            if (str.charAt(i) == PLUS || str.charAt(i) == MINUS){
//                return i;
//            }
//            if (!foundAlt && (str.charAt(i) == MUL || str.charAt(i) == DIV)){
//                alternative = i;
//                foundAlt = true;
//            }
//        }
//        return alternative;
//    }
//
//
//    private static Map<Integer, Integer> getBlocks(String... parts) {
//        Map<Integer,Integer> blocks = new HashMap<Integer, Integer>();
//        Stack<Integer> open = new Stack<Integer>(), close = new Stack<Integer>();
//        int position = 0;
//        for (String part : parts){
//            for (int i = 0; i<part.length(); i++){
//                switch (part.charAt(i)){
//                    case OPEN:
//                        open.push(i); position++;
//                        break;
//                    case CLOSE:
//                        blocks.put(open.pop(),i);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//        return blocks;
//    }
//
//    public static void main(String... args){
//        parseBoolExpr("-7 -(2*x +4)/2 -z*(1 + (y/4 -2)) != 3x");
//    }
//
//    private static List<String> getVars(String... parts){
//        List<String> vars = new ArrayList<String>();
//        for (String part: parts){
//            String[] vs = (new String(part)).split(opsAndNums);
//            for (int i =0; i<vs.length; i++){
//                if(vs[i].equals("")) continue;
//                vars.add(vs[i]);
//            }
//        }
//        return vars;
//    }
//

}
