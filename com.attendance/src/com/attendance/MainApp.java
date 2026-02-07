package com.attendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The main class for the Attendance System.
 * Creates the Swing UI, manages state, and integrates all components without voice recognition.
 */
public class MainApp {

    // --- UI Components ---
    private JFrame frame;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JLabel dateLabel;
    private JLabel statusLabel;

    // --- State ---
    private LocalDate currentDate;
    private DatabaseManager dbManager;

    public static void main(String[] args) {
        // Use invokeLater to ensure thread safety for Swing components
        SwingUtilities.invokeLater(() -> {
            try {
                // Set a modern look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new MainApp().createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Could not start the application.\nError: " + e.getMessage(), "Application Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void createAndShowGUI() {
        frame = new JFrame("Attendance Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Center the frame

        dbManager = new DatabaseManager();
        currentDate = LocalDate.now();

        // --- Main Panel ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Header Panel ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Attendance System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);

        // --- Date Navigation Panel ---
        JPanel datePanel = new JPanel();
        JButton prevDayButton = new JButton("< Prev Day");
        dateLabel = new JLabel(currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton nextDayButton = new JButton("Next Day >");
        datePanel.add(prevDayButton);
        datePanel.add(dateLabel);
        datePanel.add(nextDayButton);

        // --- Center Panel (Table) ---
        String[] columnNames = {"Roll No", "Name", "Status", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only the "Actions" column is editable
            }
        };
        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(30);
        attendanceTable.getColumn("Actions").setCellRenderer(new ButtonPanelRenderer());
        attendanceTable.getColumn("Actions").setCellEditor(new ButtonPanelEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);

        // --- Bottom Control Panel ---
        JPanel controlPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Status: Idle");
        JButton manageRosterButton = new JButton("Manage Roster");
        JButton exportButton = new JButton("Export Full Report");
        JButton marksButton = new JButton("Analysis & Marks");

        JPanel buttonGroup = new JPanel();
        buttonGroup.add(manageRosterButton);
        buttonGroup.add(exportButton);
        buttonGroup.add(marksButton);

        controlPanel.add(statusLabel, BorderLayout.WEST);
        controlPanel.add(buttonGroup, BorderLayout.EAST);

        // --- Assemble Main Panel ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(datePanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        prevDayButton.addActionListener(e -> changeDay(-1));
        nextDayButton.addActionListener(e -> changeDay(1));
        manageRosterButton.addActionListener(e -> manageRoster());
        exportButton.addActionListener(e -> exportFullReport());
        marksButton.addActionListener(e -> showAnalysisAndMarks());

        // --- Finalize Frame ---
        frame.setContentPane(mainPanel);
        frame.setVisible(true);

        // Initial data load
        refreshTable();
    }

    private void changeDay(int amount) {
        currentDate = currentDate.plusDays(amount);
        dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0); // Clear existing rows
        List<Student> students = dbManager.getAllStudents();
        Map<Integer, String> attendanceMap = dbManager.getAttendanceForDate(currentDate);

        for (Student student : students) {
            String status = attendanceMap.getOrDefault(student.getRoll(), "Pending");
            tableModel.addRow(new Object[]{student.getRoll(), student.getName(), status, ""});
        }
    }

    private void updateAttendanceStatus(int roll, String status) {
        dbManager.markAttendance(roll, currentDate, status);
        statusLabel.setText("Marked Roll " + roll + " as " + status);
        refreshTable();
    }

    private void manageRoster() {
        JDialog rosterDialog = new JDialog(frame, "Manage Roster", true);
        // This can be expanded to add/remove students.
        JOptionPane.showMessageDialog(rosterDialog, "Functionality to add/remove students goes here.");
    }

    private void exportFullReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Full Report");
        fileChooser.setSelectedFile(new File("full_attendance_report.csv"));
        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                // Write header
                writer.append("Roll No,Name,Date,Status\n");

                // Write data
                List<Student> students = dbManager.getAllStudents();
                Map<LocalDate, Map<Integer, String>> fullReport = dbManager.getFullAttendanceReport();

                for (Student student : students) {
                    for (Map.Entry<LocalDate, Map<Integer, String>> entry : fullReport.entrySet()) {
                        LocalDate date = entry.getKey();
                        String status = entry.getValue().getOrDefault(student.getRoll(), "Pending");
                        writer.append(String.format("%d,%s,%s,%s\n",
                                student.getRoll(),
                                student.getName(),
                                date.toString(),
                                status));
                    }
                }
                JOptionPane.showMessageDialog(frame, "Report exported successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error exporting report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAnalysisAndMarks() {
         JDialog analysisDialog = new JDialog(frame, "Analysis & Marks", true);
        // ... Implementation for analysis and marks dialog ...
        // This can be expanded to show charts and manage marks.
        JOptionPane.showMessageDialog(analysisDialog, "Functionality for attendance analysis and marks management goes here.");
    }

    // --- Inner classes for table button rendering ---

    class ButtonPanelRenderer extends JPanel implements TableCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final JButton presentButton = new JButton("Present");
        final JButton absentButton = new JButton("Absent");

        public ButtonPanelRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            add(presentButton);
            add(absentButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonPanelEditor extends DefaultCellEditor {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected JPanel panel;
        protected JButton presentButton;
        protected JButton absentButton;
        private int currentRoll;

        public ButtonPanelEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            presentButton = new JButton("Present");
            absentButton = new JButton("Absent");

            panel.add(presentButton);
            panel.add(absentButton);

            presentButton.addActionListener(e -> {
                updateAttendanceStatus(currentRoll, "Present");
                fireEditingStopped();
            });

            absentButton.addActionListener(e -> {
                updateAttendanceStatus(currentRoll, "Absent");
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRoll = (int) table.getValueAt(row, 0);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
