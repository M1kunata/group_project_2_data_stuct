package Project2_6713221;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;
class light
{
    private int row,col;
    private boolean broken,on;
    public light(int r,int c,boolean temp){
        row=r;
        col=c;
        on = temp;
    }
    public void setBroken() {broken=true;}
    public void toggle() {on=!on;}
    public int getRow() {return row;}
    public int getCol() {return col;}
    public boolean getbroken() {return broken;}
    public boolean checkon() {return on;}
}
class BoardState {
    String bits;               // เช่น "110101..."
    List<String> history;      // เช่น ["0,0", "1,2"]

    public BoardState(String bits, List<String> history) {
        this.bits = bits;
        this.history = history;
    }
}
class lightboard{
    private Graph<light, DefaultWeightedEdge> overboard ;
    private light[][] node;
    private int board;
    public lightboard(int size,String initial){
        board = size;
        overboard =  new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        node = new light[size][size];
        for(int i=0;i<board;i++)
            for(int j=0;j<board;j++)
            {
                boolean isOn = initial.charAt(i * board + j) == '1';
                node[i][j] = new light(i,j,isOn);
                overboard.addVertex(node[i][j]);
            }
        for (int r = 0; r < board; r++) {
            for (int c = 0; c < board; c++) {
                createEdgesForNode(node[r][c]);
            }
        }
    }
    // เพิ่มใน class lightboard
    public void solveBFS(String initialBits) {
        Queue<BoardState> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        // 1. เริ่มต้น: ใส่สถานะแรกลงไป
        queue.add(new BoardState(initialBits, new ArrayList<>()));
        visited.add(initialBits);
            // 3. ถ้ายังไม่จบ ลองกดทุกปุ่มที่เป็นไปได้ (0,0 ถึง n,n)
            for (int r = 0; r < board; r++) {
                for (int c = 0; c < board; c++) {
                    String nextBits = simulatePress(current.bits, r, c);

                    if (!visited.contains(nextBits)) {
                        visited.add(nextBits);

                        // เก็บประวัติการกด
                        List<String> nextHistory = new ArrayList<>(current.history);
                        nextHistory.add(r + "," + c);

                        queue.add(new BoardState(nextBits, nextHistory));
                    }
                }
            }
    }

    // เมธอดจำลองการเปลี่ยนค่า String bits เมื่อมีการกดปุ่ม (r,c)
    private String simulatePress(String currentBits, int r, int c) {
        char[] bits = currentBits.toCharArray();

        // กดตัวมันเอง
        toggleBit(bits, r, c);

        // กดเพื่อนบ้าน (ดึงมาจาก Edge ใน JGraphT ที่คุณสร้างไว้)
        light source = node[r][c];
        for (DefaultWeightedEdge e : overboard.outgoingEdgesOf(source)) {
            light neighbor = overboard.getEdgeTarget(e);
            toggleBit(bits, neighbor.getRow(), neighbor.getCol());
        }

        return new String(bits);
    }
    private void createEdgesForNode(light node) {
        int r = node.getRow();
        int c = node.getCol();

        if (!node.getbroken()) {
            // กรณีไฟปกติ: ตรวจสอบเพื่อนบ้านบน ล่าง ซ้าย ขวา
            int[][] neighbors = {{r-1, c}, {r+1, c}, {r, c-1}, {r, c+1}};
            addValidEdges(node, neighbors);
        } else {
            // กรณีไฟเสีย: ตรวจสอบเพื่อนบ้านแนวทแยง 4 ทิศ
            int[][] diagonals = {{r-1, c-1}, {r-1, c+1}, {r+1, c-1}, {r+1, c+1}};
            addValidEdges(node, diagonals);
        }
    }

    private void addValidEdges(light source, int[][] targets) {
        for (int[] pos : targets) {
            int tr = pos[0], tc = pos[1];
            if (tr >= 0 && tr < board && tc >= 0 && tc < board) {
                overboard.addEdge(source, node[tr][tc]);
            }
        }
    }
    public void show()//gen ma
    {
        // พิมพ์เลขคอลัมน์ด้านบน (Header)
        System.out.print("      "); // เว้นช่องว่างสำหรับคำว่า row
        for (int c = 0; c < board; c++) {
            System.out.printf("| col %d ", c);
        }
        System.out.println("|");

        // พิมพ์ข้อมูลในแต่ละแถว
        for (int i = 0; i < board; i++) {
            System.out.printf("row %d |", i); // พิมพ์เลขแถวด้านซ้าย
            for (int j = 0; j < board; j++) {
                // ดึงข้อมูลจาก Object light ใน Array
                int status = node[i][j].checkon() ? 1 : 0;
                String mark = node[i][j].getbroken() ? "x" : " ";

                // แสดงผลในรูปแบบ " 1  " หรือ " 1x " ตามเงื่อนไขไฟเสีย [cite: 19, 125]
                System.out.printf("  %d%s  |", status, mark);
            }
            System.out.println(); // ขึ้นบรรทัดใหม่เมื่อจบแถว
        }
    }
    public void setBroken(int row,int col)
    {
        node[row][col].setBroken();
        for (int r = 0; r < board; r++) {
            for (int c = 0; c < board; c++) {
                createEdgesForNode(node[r][c]);
            }
        }
    }
}
public class main { //ยังไม่ได้กั้น error index out of bound จากการรับinput ใดๆ
    static public void main(String[] arg)
    {
        main mainapp = new main();
        mainapp.start();
    }
    public void start()
    {
        Scanner in = new Scanner(System.in);
        System.out.printf("start:");
        int b = in.nextInt();//ฝากกั้น errorด้วย
        in.nextLine();
        create(b,in);

    }
    public void create(int board,Scanner in) {
        System.out.printf("initial state (row first) left to right :");
        String prepare = in.nextLine();
        lightboard A = new lightboard(board,prepare);
        A.show();
        System.out.printf("Set broken light (Y/N)?");
        String select = in.nextLine();
        if(select.equalsIgnoreCase("y"))
        {
            System.out.printf("Enter row of broken light (0-%d) = ",board);
            int row = in.nextInt();
            System.out.printf("Enter col of broken light (0-%d) = ",board);
            int col = in.nextInt();
            A.setBroken(row,col);
        }
        A.show();
        A.solveBFS(prepare);
    }
}
