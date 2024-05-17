import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.*;

public class TimeTable extends JFrame implements ActionListener {

    private JPanel screen = new JPanel(), tools = new JPanel();
    private JButton tool[];
    private JTextField field[];
    private CourseArray courses;
    private Color CRScolor[] = {Color.RED, Color.GREEN, Color.BLACK};
    private int currentIteration = 1;
    private BufferedWriter logWriter;
    private Autoassociator autoassociator;



    public TimeTable() {
        super("Dynamic Time Table");
        setSize(500, 800);
        setLayout(new FlowLayout());

        screen.setPreferredSize(new Dimension(400, 800));
        add(screen);

        setTools();
        add(tools);

        setVisible(true);

        try {
            logWriter = new BufferedWriter(new FileWriter("TimeTableLog.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTools() {
        String capField[] = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
        field = new JTextField[capField.length];

        String capButton[] = {"Load", "Start", "Step", "Print", "Exit", "Continue"};
        tool = new JButton[capButton.length];

        tools.setLayout(new GridLayout(2 * capField.length + capButton.length, 1));

        for (int i = 0; i < field.length; i++) {
            tools.add(new JLabel(capField[i]));
            field[i] = new JTextField(5);
            tools.add(field[i]);
        }

        for (int i = 0; i < tool.length; i++) {
            tool[i] = new JButton(capButton[i]);
            tool[i].addActionListener(this);
            tools.add(tool[i]);
        }

        field[0].setText("17");
        field[1].setText("381");
        field[2].setText("sta-f-83.stu");
        field[3].setText("1");
    }




    public void draw() {
        Graphics g = screen.getGraphics();
        int width = Integer.parseInt(field[0].getText()) * 10;
        for (int courseIndex = 1; courseIndex < courses.length(); courseIndex++) {
            g.setColor(CRScolor[courses.status(courseIndex) > 0 ? 0 : 1]);
            g.drawLine(0, courseIndex, width, courseIndex);
            g.setColor(CRScolor[CRScolor.length - 1]);
            g.drawLine(10 * courses.slot(courseIndex), courseIndex, 10 * courses.slot(courseIndex) + 10, courseIndex);
        }
    }

    private int getButtonIndex(JButton source) {
        int result = 0;
        while (source != tool[result]) result++;
        return result;
    }

    public void actionPerformed(ActionEvent click) {
        int min, step, clashes;

        switch (getButtonIndex((JButton) click.getSource())) {
            case 0:
                int slots = Integer.parseInt(field[0].getText());
                courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, slots);
                courses.readClashes(field[2].getText());
                autoassociator = new Autoassociator(courses);
                trainAutoassociator();
                draw();
                break;
            case 1:
                min = Integer.MAX_VALUE;
                step = 0;
                for (int i = 1; i < courses.length(); i++) courses.setSlot(i, 0);

                for (int iteration = 1; iteration <= Integer.parseInt(field[3].getText()); iteration++) {
                    courses.iterate(Integer.parseInt(field[4].getText()));
                    applyAutoassociatorUpdates();
                    draw();
                    clashes = courses.clashesLeft();
                    logState(iteration, clashes);
                    if (clashes < min) {
                        min = clashes;
                        step = iteration;
                    }
                }
                System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
                setVisible(true);
                break;
            case 2:
                courses.iterate(Integer.parseInt(field[4].getText()));
                applyAutoassociatorUpdates();
                draw();
                break;
            case 3:
                System.out.println("Exam\tSlot\tClashes");
                for (int i = 1; i < courses.length(); i++)
                    System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
                break;
            case 4:
                System.exit(0);
                break;
            case 5:
                courses.iterate(Integer.parseInt(field[4].getText()));
                applyAutoassociatorUpdates();
                draw();
                break;
//                min = Integer.MAX_VALUE;
//                step = 0;
//                for (int i = 1; i < courses.length(); i++) courses.setSlot(i, 0);
//                for (int iteration = currentIteration; iteration <= Integer.parseInt(field[3].getText()); iteration++) {
//                    courses.iterate(Integer.parseInt(field[4].getText()));
//                    draw();
//                    clashes = courses.clashesLeft();
//                    if (clashes < min) {
//                        min = clashes;
//                        step = iteration;
//                    }
//                }
//                System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
//                currentIteration = step + 1;
//                setVisible(true);
//                break;
        }
    }


    private void logState(int iteration, int clashes) {
        try {
            logWriter.write("Iteration: " + iteration + ", Clashes: " + clashes + "\n");
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void trainAutoassociator() {
        try {
            for (int i = 1; i < courses.length(); i++) {
                if (courses.status(i) == 0) {
                    int[] timeslotPattern = courses.getTimeSlot(i);
                    autoassociator.training(timeslotPattern);
                    logWriter.write("Trained on timeslot: " + i + ", Pattern: " + Arrays.toString(timeslotPattern) + "\n");
                }
            }
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyAutoassociatorUpdates() {
        try {
            for (int i = 1; i < courses.length(); i++) {
                int[] currentTimeslot = courses.getTimeSlot(i);
                autoassociator.fullUpdate(currentTimeslot);
                logWriter.write("Applied Autoassociator update to timeslot: " + i + ", Updated Pattern: " + Arrays.toString(currentTimeslot) + "\n");
                logWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TimeTable();
    }
}



