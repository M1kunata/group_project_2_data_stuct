package Project2_6713221;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

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
    private DefaultDirectedGraph<light, DefaultEdge> overboard ;
    private light[][] node;
    private int board;
    public lightboard(int size,String initial){
        board = size;
        overboard =  new DefaultDirectedGraph<>(DefaultEdge.class);
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
    public void solveBFS(String initialBits) {
        Queue<BoardState> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        // สร้างเป้าหมาย: สตริงที่มีแต่ '0' ตามขนาดกระดาน
        StringBuilder targetBuilder = new StringBuilder();
        for (int i = 0; i < board * board; i++) {
            targetBuilder.append("0");
        }
        String targetBits = targetBuilder.toString();

        // 1. เริ่มต้น: ใส่สถานะแรกลงไป
        queue.add(new BoardState(initialBits, new ArrayList<>()));
        visited.add(initialBits);

        System.out.println("\nStart searching for solution (BFS)...");

        // 2. เริ่มรัน BFS
        while (!queue.isEmpty()) {
            BoardState current = queue.poll();

            // เช็คว่าสถานะปัจจุบันคือไฟดับหมดหรือยัง
            if (current.bits.equals(targetBits)) {
                System.out.println(current.history.size()+" move to turn off all lights");
                for (int i=0;i<current.history.size();i++)
                {
                    String[] rowandcol = current.history.get(i).substring(1,4).split(",");
                    System.out.println(">> Move "+(i+1)+" turn off row "+rowandcol[0]+", col "+rowandcol[1]);
                    int row = Integer.parseInt(rowandcol[0]);
                    int col = Integer.parseInt(rowandcol[1]);
                    node[row][col].toggle();
                    for (DefaultEdge e : overboard.outgoingEdgesOf(node[row][col])) {
                        light neighbor = overboard.getEdgeTarget(e);
                        neighbor.toggle();
                    }
                    show();
                }
                return ; // จบการทำงานเพราะเจอคำตอบที่สั้นที่สุดแล้ว
            }
            // 3. ถ้ายังไม่จบ ลองกดทุกปุ่มที่เป็นไปได้ (0,0 ถึง n-1,n-1)
            for (int r = 0; r < board; r++) {
                for (int c = 0; c < board; c++) {
                    String nextBits = simulatePress(current.bits, r, c);
                    if (!visited.contains(nextBits)) {
                        visited.add(nextBits);
                        // เก็บประวัติการกด (เก็บเป็นพิกัด r,c)
                        List<String> nextHistory = new ArrayList<>(current.history);
                        nextHistory.add("(" + r + "," + c + ")");
                        queue.add(new BoardState(nextBits, nextHistory));
                    }
                }
            }
        }
        // ถ้าคิวว่างแล้วยังไม่เจอคำตอบ
        System.out.println("No solution!!");
    }

    // เมธอดจำลองการเปลี่ยนค่า String bits เมื่อมีการกดปุ่ม (r,c)
    private String simulatePress(String currentBits, int r, int c) {
        char[] bits = currentBits.toCharArray();
        // 1. กดสลับไฟที่ตัวมันเอง
        toggleChar(bits, r, c);
        // 2. ดึง Node ปัจจุบัน
        light source = node[r][c];
        // 3. กดเพื่อนบ้าน (ดึงมาจาก Edge ใน JGraphT ที่คุณสร้างไว้)
        // ใช้ Graphs.neighborListOf เพื่อดึงเพื่อนบ้านที่เชื่อมด้วย Edge ทั้งหมดมาได้เลย
        for (DefaultEdge e : overboard.outgoingEdgesOf(source)) {
            light neighbor = overboard.getEdgeTarget(e);
            toggleChar(bits, neighbor.getRow(), neighbor.getCol());
        }
        return new String(bits);
    }

    // เมธอดช่วยสลับตัวอักษร '0' เป็น '1' และ '1' เป็น '0'
    private void toggleChar(char[] bits, int r, int c) {
        int index = r * board + c;
        bits[index] = (bits[index] == '1') ? '0' : '1';
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
            System.out.printf("| col %d", c);
        }
        System.out.println("|");
        // พิมพ์ข้อมูลในแต่ละแถว
        for (int i = 0; i < board; i++) {
            System.out.printf("row %d |", i); // พิมพ์เลขแถวด้านซ้าย
            for (int j = 0; j < board; j++) {
                // ดึงข้อมูลจาก Object light ใน Array
                int status;
                if(node[i][j].checkon())
                    status=1;
                else status=0;
                String mark = node[i][j].getbroken() ? "x" : " ";
                // แสดงผลในรูปแบบ " 1  " หรือ " 1x " ตามเงื่อนไขไฟเสีย [cite: 19, 125]
                System.out.printf("  %d%s  |", status, mark);
            }
            System.out.println(); // ขึ้นบรรทัดใหม่เมื่อจบแถว
        }
    }
    public void setBroken(int row, int col) {
        // 1. เปลี่ยนสถานะของหลอดไฟให้เป็น "เสีย"
        node[row][col].setBroken();
        // 2. ดึงเส้นเชื่อม (Edge) "ขาออก" เดิมทั้งหมดของโหนดนี้มาเก็บไว้ใน Set
        Set<DefaultEdge> edgesToRemove = new HashSet<>(overboard.outgoingEdgesOf(node[row][col]));
        // 3. ลบเส้นเชื่อมขาออกเดิมเหล่านั้นทิ้งไปจากกราฟ
        overboard.removeAllEdges(edgesToRemove);
        // 4. สร้างเส้นเชื่อมใหม่สำหรับโหนดนี้ (ซึ่งจะกลายเป็นเส้นแนวทแยง)
        createEdgesForNode(node[row][col]);
    }
}
public class main {
    static public void main(String[] arg) {
        main mainapp = new main();
        mainapp.start();
    }

    public void start() {
        Scanner in = new Scanner(System.in);
        int b = -1;
        while (b < 2) {
            System.out.print("Enter grid size N (N x N, min 2): ");
            String line = in.nextLine().trim();
            try {
                b = Integer.parseInt(line);
                if (b < 2) System.out.println("  [!] N must be at least 2.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid integer.");
            }
        }
        create(b, in);
    }

    public void create(int board, Scanner in) {
        // รับ initial state
        String prepare = "";
        while (true) {
            System.out.printf("Enter initial state (%d bits, left to right, e.g. %s): ",
                    board * board, exampleBits(board));
            prepare = in.nextLine().trim();
            if (prepare.length() == board * board && prepare.matches("[01]+")) break;
            System.out.println("  [!] Please enter exactly " + (board * board)
                    + " characters using only '0' and '1'.");
        }

        lightboard A = new lightboard(board, prepare);
        A.show();

        // ถามไฟเสีย
        System.out.print("Set broken light (Y/N)? ");
        String select = in.nextLine().trim();
        while (!select.equalsIgnoreCase("y") && !select.equalsIgnoreCase("n")) {
            System.out.print("  [!] Please enter Y or N: ");
            select = in.nextLine().trim();
        }

        if (select.equalsIgnoreCase("y")) {
            int row = readInt(in,
                    "Enter row of broken light (0-" + (board - 1) + ") = ",
                    0, board - 1,
                    "  [!] Row must be between 0 and " + (board - 1) + ".");
            int col = readInt(in,
                    "Enter col of broken light (0-" + (board - 1) + ") = ",
                    0, board - 1,
                    "  [!] Col must be between 0 and " + (board - 1) + ".");
            A.setBroken(row, col);
        }

        A.show();

        // Solve
        List<int[]> solution = A.solveBFS(prepare);
        if (solution == null) {
            System.out.println("No solution !!");
        } else {
            System.out.println(solution.size() + " moves to turn off all lights");
            // replay + แสดง
            String currentBits = prepare;
            int brokenR = A.getBrokenR();
            int brokenC = A.getBrokenC();
            for (int step = 0; step < solution.size(); step++) {
                int r = solution.get(step)[0];
                int c = solution.get(step)[1];
                System.out.printf("%n>>> Move %d : turn %s row %d, col %d%n",
                        step + 1,
                        currentBits.charAt(r * board + c) == '1' ? "off" : "on",
                        r, c);
                currentBits = replayPress(currentBits, r, c, board, brokenR, brokenC);
                System.out.println("States in bits = " + currentBits);
                A.show();
            }
        }

        // ถามเล่นใหม่
        System.out.print("\nPlay again? (Y/N): ");
        String again = in.nextLine().trim();
        while (!again.equalsIgnoreCase("y") && !again.equalsIgnoreCase("n")) {
            System.out.print("  [!] Please enter Y or N: ");
            again = in.nextLine().trim();
        }
        if (again.equalsIgnoreCase("y")) start();
        A.solveBFS(prepare);

    }

    private int readInt(Scanner in, String prompt, int min, int max, String errorMsg) {
        while (true) {
            System.out.print(prompt);
            String line = in.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.println(errorMsg);
            } catch (NumberFormatException e) {
                System.out.println(errorMsg);
            }
        }
    }

    private String replayPress(String bits, int r, int c, int n, int brokenR, int brokenC) {
        char[] arr = bits.toCharArray();
        flipBit(arr, r, c, n);
        boolean isBroken = (r == brokenR && c == brokenC);
        int[][] dirs = isBroken
                ? new int[][]{{r-1,c-1},{r-1,c+1},{r+1,c-1},{r+1,c+1}}
                : new int[][]{{r-1,c},{r+1,c},{r,c-1},{r,c+1}};
        for (int[] nb : dirs)
            if (nb[0] >= 0 && nb[0] < n && nb[1] >= 0 && nb[1] < n)
                flipBit(arr, nb[0], nb[1], n);
        return new String(arr);
    }

    private void flipBit(char[] arr, int r, int c, int n) {
        int i = r * n + c;
        arr[i] = arr[i] == '0' ? '1' : '0';
    }

    private String exampleBits(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n * n; i++) sb.append(i % 3 == 0 ? '1' : '0');
        return sb.toString();
    }
}