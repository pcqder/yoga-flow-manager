package com.yoga;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class YogaManager {
    private static final String DATA_FILE = "yoga_data.dat";
    private List<Student> students = new ArrayList<>();
    private List<YogaClass> classes = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter inputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        YogaManager app = new YogaManager();
        app.load();
        app.run();
    }

    private void run() {
        System.out.println("\n=========================================");
        System.out.println("   Yoga Studio Manager");
        System.out.println("=========================================");
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": addStudent(); break;
                case "2": listStudents(); break;
                case "3": markAttendance(); break;
                case "4": markPayment(); break;
                case "5": removeStudent(); break;
                case "6": addClass(); break;
                case "7": listClasses(true); break;
                case "8": listClasses(false); break;
                case "9": enrollInClass(); break;
                case "10": cancelClass(); break;
                case "11": monthlyReport(); break;
                case "12": resetMonth(); break;
                case "0":
                    save();
                    System.out.println("Saved. Namaste.");
                    return;
                default: System.out.println("Invalid choice.");
            }
            save();
        }
    }

    private void printMenu() {
        System.out.println("\n----- Menu -----");
        System.out.println("STUDENTS                       CLASSES");
        System.out.println(" 1. Add student                 6. Schedule class");
        System.out.println(" 2. List students               7. List upcoming classes");
        System.out.println(" 3. Mark attendance             8. List all classes");
        System.out.println(" 4. Mark payment                9. Enroll student in class");
        System.out.println(" 5. Remove student             10. Cancel class");
        System.out.println("REPORTS");
        System.out.println("11. Monthly report             12. Reset month (new billing cycle)");
        System.out.println(" 0. Save & exit");
        System.out.print("Choose: ");
    }

    // ---------- Students ----------
    private void addStudent() {
        System.out.print("Name: "); String n = scanner.nextLine().trim();
        if (n.isEmpty()) { System.out.println("Cancelled."); return; }
        System.out.print("Email: "); String e = scanner.nextLine().trim();
        System.out.print("Phone: "); String p = scanner.nextLine().trim();
        System.out.print("Membership (Monthly/Drop-in/Unlimited): "); String m = scanner.nextLine().trim();
        System.out.print("Monthly fee: "); double f = parseDouble(scanner.nextLine().trim(), 0);
        students.add(new Student(n, e, p, m, f));
        System.out.println("Student added.");
    }

    private void listStudents() {
        if (students.isEmpty()) { System.out.println("No students yet."); return; }
        System.out.println("\n# | Details");
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i));
        }
    }

    private Student pickStudent() {
        listStudents();
        if (students.isEmpty()) return null;
        System.out.print("Pick student #: ");
        int i = parseInt(scanner.nextLine().trim(), 0) - 1;
        if (i < 0 || i >= students.size()) { System.out.println("Invalid."); return null; }
        return students.get(i);
    }

    private void markAttendance() {
        Student s = pickStudent();
        if (s != null) { s.incrementAttendance(); System.out.println("Attendance recorded for " + s.getName()); }
    }

    private void markPayment() {
        Student s = pickStudent();
        if (s != null) { s.setPaidThisMonth(true); System.out.println("Marked PAID: " + s.getName()); }
    }

    private void removeStudent() {
        Student s = pickStudent();
        if (s != null) { students.remove(s); System.out.println("Removed " + s.getName()); }
    }

    // ---------- Classes ----------
    private void addClass() {
        System.out.print("Class title (e.g. Vinyasa Flow): "); String t = scanner.nextLine().trim();
        if (t.isEmpty()) { System.out.println("Cancelled."); return; }
        System.out.print("Date & time (yyyy-MM-dd HH:mm): ");
        LocalDateTime dt;
        try { dt = LocalDateTime.parse(scanner.nextLine().trim(), inputFmt); }
        catch (DateTimeParseException ex) { System.out.println("Bad date format."); return; }
        System.out.print("Location: "); String loc = scanner.nextLine().trim();
        System.out.print("Capacity: "); int cap = parseInt(scanner.nextLine().trim(), 10);
        System.out.print("Notes (optional): "); String notes = scanner.nextLine().trim();
        classes.add(new YogaClass(t, dt, loc, cap, notes));
        System.out.println("Class scheduled.");
    }

    private void listClasses(boolean futureOnly) {
        List<YogaClass> view = new ArrayList<>();
        for (YogaClass c : classes) if (!futureOnly || c.isFuture()) view.add(c);
        view.sort(Comparator.comparing(YogaClass::getDateTime));
        if (view.isEmpty()) { System.out.println(futureOnly ? "No upcoming classes." : "No classes."); return; }
        System.out.println();
        for (int i = 0; i < view.size(); i++) System.out.println((i + 1) + ". " + view.get(i));
    }

    private YogaClass pickClass() {
        List<YogaClass> sorted = new ArrayList<>(classes);
        sorted.sort(Comparator.comparing(YogaClass::getDateTime));
        if (sorted.isEmpty()) { System.out.println("No classes."); return null; }
        for (int i = 0; i < sorted.size(); i++) System.out.println((i + 1) + ". " + sorted.get(i));
        System.out.print("Pick class #: ");
        int i = parseInt(scanner.nextLine().trim(), 0) - 1;
        if (i < 0 || i >= sorted.size()) { System.out.println("Invalid."); return null; }
        return sorted.get(i);
    }

    private void enrollInClass() {
        YogaClass c = pickClass(); if (c == null) return;
        Student s = pickStudent(); if (s == null) return;
        if (c.enroll(s.getName())) System.out.println("Enrolled " + s.getName() + " in " + c.getTitle());
        else System.out.println("Could not enroll (full or already enrolled).");
    }

    private void cancelClass() {
        YogaClass c = pickClass();
        if (c != null) { classes.remove(c); System.out.println("Cancelled " + c.getTitle()); }
    }

    // ---------- Reports ----------
    private void monthlyReport() {
        System.out.println("\n========= MONTHLY REPORT =========");
        System.out.println("Total students: " + students.size());
        double expected = 0, collected = 0;
        int totalAttendance = 0, unpaid = 0;
        for (Student s : students) {
            expected += s.getMonthlyFee();
            if (s.isPaidThisMonth()) collected += s.getMonthlyFee(); else unpaid++;
            totalAttendance += s.getClassesAttendedThisMonth();
        }
        System.out.printf("Expected revenue:  $%.2f%n", expected);
        System.out.printf("Collected:         $%.2f%n", collected);
        System.out.printf("Outstanding:       $%.2f (%d students)%n", expected - collected, unpaid);
        System.out.println("Total attendance:  " + totalAttendance);

        long upcoming = classes.stream().filter(YogaClass::isFuture).count();
        System.out.println("Upcoming classes:  " + upcoming);

        if (unpaid > 0) {
            System.out.println("\nUnpaid students:");
            for (Student s : students) if (!s.isPaidThisMonth())
                System.out.println("  - " + s.getName() + " (" + s.getEmail() + ")");
        }
        System.out.println("==================================");
    }

    private void resetMonth() {
        System.out.print("Reset all attendance & payment status for new month? (yes/no): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            for (Student s : students) s.resetMonthly();
            System.out.println("Monthly counters reset.");
        }
    }

    // ---------- Persistence ----------
    @SuppressWarnings("unchecked")
    private void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            students = (List<Student>) in.readObject();
            classes = (List<YogaClass>) in.readObject();
            System.out.println("Loaded " + students.size() + " students and " + classes.size() + " classes.");
        } catch (Exception e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    private void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(students);
            out.writeObject(classes);
        } catch (IOException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }

    // ---------- Helpers ----------
    private int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private double parseDouble(String s, double def) { try { return Double.parseDouble(s); } catch (Exception e) { return def; } }
}
