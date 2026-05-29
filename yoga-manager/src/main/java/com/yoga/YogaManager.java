package com.yoga;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class YogaManager {
    private static final String DATA_FILE = "yoga_data.dat";
    private static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("EEE, MMM d yyyy 'at' HH:mm");

    private static List<Student> students = new ArrayList<>();
    private static List<YogaClass> classes = new ArrayList<>();

    public static void main(String[] args) {
        load();

        Javalin app = Javalin.create(cfg -> {
            cfg.showJavalinBanner = false;
        }).start(7070);

        System.out.println("\n🧘  Yoga Studio Manager running at http://localhost:7070\n");

        app.get("/", YogaManager::dashboard);

        // Students
        app.get("/students", YogaManager::studentsPage);
        app.post("/students/add", YogaManager::addStudent);
        app.post("/students/{idx}/attend", ctx -> { studentAction(ctx, "attend"); ctx.redirect("/students"); });
        app.post("/students/{idx}/pay", ctx -> { studentAction(ctx, "pay"); ctx.redirect("/students"); });
        app.post("/students/{idx}/unpay", ctx -> { studentAction(ctx, "unpay"); ctx.redirect("/students"); });
        app.post("/students/{idx}/delete", ctx -> { studentAction(ctx, "delete"); ctx.redirect("/students"); });
        app.post("/reset-month", ctx -> {
            for (Student s : students) s.resetMonthly();
            save();
            ctx.redirect("/");
        });

        // Classes
        app.get("/classes", YogaManager::classesPage);
        app.post("/classes/add", YogaManager::addClass);
        app.post("/classes/{idx}/enroll", YogaManager::enroll);
        app.post("/classes/{idx}/unenroll", YogaManager::unenroll);
        app.post("/classes/{idx}/delete", ctx -> {
            int i = Integer.parseInt(ctx.pathParam("idx"));
            if (i >= 0 && i < classes.size()) { classes.remove(i); save(); }
            ctx.redirect("/classes");
        });
    }

    // ============ PAGES ============

    private static void dashboard(Context ctx) {
        double expected = 0, collected = 0;
        int attendance = 0, unpaid = 0;
        for (Student s : students) {
            expected += s.getMonthlyFee();
            if (s.isPaidThisMonth()) collected += s.getMonthlyFee(); else unpaid++;
            attendance += s.getClassesAttendedThisMonth();
        }
        long upcoming = classes.stream().filter(YogaClass::isFuture).count();

        StringBuilder body = new StringBuilder();
        body.append("<h1>Dashboard</h1>");
        body.append("<div class='grid'>");
        body.append(statCard("Students", String.valueOf(students.size())));
        body.append(statCard("Upcoming classes", String.valueOf(upcoming)));
        body.append(statCard("Attendance this month", String.valueOf(attendance)));
        body.append(statCard("Expected revenue", String.format("$%.2f", expected)));
        body.append(statCard("Collected", String.format("$%.2f", collected)));
        body.append(statCard("Outstanding", String.format("$%.2f (%d)", expected - collected, unpaid)));
        body.append("</div>");

        if (unpaid > 0) {
            body.append("<h2>Unpaid students</h2><ul>");
            for (Student s : students) if (!s.isPaidThisMonth())
                body.append("<li>").append(esc(s.getName())).append(" — ").append(esc(s.getEmail())).append("</li>");
            body.append("</ul>");
        }

        body.append("<h2>Next 5 classes</h2>");
        List<YogaClass> upcomingList = new ArrayList<>();
        for (YogaClass c : classes) if (c.isFuture()) upcomingList.add(c);
        upcomingList.sort(Comparator.comparing(YogaClass::getDateTime));
        if (upcomingList.isEmpty()) body.append("<p class='muted'>None scheduled.</p>");
        else {
            body.append("<ul>");
            for (int i = 0; i < Math.min(5, upcomingList.size()); i++) {
                YogaClass c = upcomingList.get(i);
                body.append("<li><strong>").append(esc(c.getTitle())).append("</strong> — ")
                    .append(c.getDateTime().format(DISPLAY_FMT)).append(" @ ").append(esc(c.getLocation()))
                    .append(" (").append(c.getEnrolledStudents().size()).append("/").append(c.getCapacity()).append(")</li>");
            }
            body.append("</ul>");
        }

        body.append("<form method='post' action='/reset-month' onsubmit=\"return confirm('Reset attendance and payments for a new month?')\">");
        body.append("<button class='danger' type='submit'>Reset Month</button></form>");

        ctx.html(layout("Dashboard", body.toString()));
    }

    private static void studentsPage(Context ctx) {
        StringBuilder body = new StringBuilder();
        body.append("<h1>Students</h1>");

        body.append("<div class='card'><h3>Add student</h3>");
        body.append("<form method='post' action='/students/add' class='form-grid'>");
        body.append(input("name", "Name", "text", true));
        body.append(input("email", "Email", "email", false));
        body.append(input("phone", "Phone", "text", false));
        body.append("<label>Membership<select name='membership'>"
                + "<option>Monthly</option><option>Drop-in</option><option>Unlimited</option></select></label>");
        body.append(input("fee", "Monthly fee ($)", "number", false));
        body.append("<button type='submit'>Add</button></form></div>");

        if (students.isEmpty()) {
            body.append("<p class='muted'>No students yet.</p>");
        } else {
            body.append("<table><thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Plan</th>"
                    + "<th>Attended</th><th>Fee</th><th>Status</th><th>Actions</th></tr></thead><tbody>");
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                body.append("<tr><td>").append(esc(s.getName())).append("</td>")
                    .append("<td>").append(esc(s.getEmail())).append("</td>")
                    .append("<td>").append(esc(s.getPhone())).append("</td>")
                    .append("<td>").append(esc(s.getMembershipType())).append("</td>")
                    .append("<td>").append(s.getClassesAttendedThisMonth()).append("</td>")
                    .append("<td>$").append(String.format("%.2f", s.getMonthlyFee())).append("</td>")
                    .append("<td>").append(s.isPaidThisMonth()
                        ? "<span class='paid'>PAID</span>" : "<span class='unpaid'>UNPAID</span>").append("</td>")
                    .append("<td class='actions'>")
                    .append(formBtn("/students/" + i + "/attend", "+ Attend", ""))
                    .append(s.isPaidThisMonth()
                        ? formBtn("/students/" + i + "/unpay", "Mark Unpaid", "")
                        : formBtn("/students/" + i + "/pay", "Mark Paid", ""))
                    .append(formBtn("/students/" + i + "/delete", "Delete", "danger"))
                    .append("</td></tr>");
            }
            body.append("</tbody></table>");
        }
        ctx.html(layout("Students", body.toString()));
    }

    private static void classesPage(Context ctx) {
        StringBuilder body = new StringBuilder();
        body.append("<h1>Classes</h1>");

        body.append("<div class='card'><h3>Schedule a class</h3>");
        body.append("<form method='post' action='/classes/add' class='form-grid'>");
        body.append(input("title", "Title (e.g. Vinyasa Flow)", "text", true));
        body.append("<label>Date & time<input type='datetime-local' name='datetime' required></label>");
        body.append(input("location", "Location", "text", false));
        body.append(input("capacity", "Capacity", "number", false));
        body.append(input("notes", "Notes", "text", false));
        body.append("<button type='submit'>Schedule</button></form></div>");

        List<YogaClass> sorted = new ArrayList<>(classes);
        sorted.sort(Comparator.comparing(YogaClass::getDateTime));

        if (sorted.isEmpty()) {
            body.append("<p class='muted'>No classes scheduled.</p>");
        } else {
            for (YogaClass c : sorted) {
                int realIdx = classes.indexOf(c);
                body.append("<div class='card class-card ").append(c.isFuture() ? "" : "past").append("'>");
                body.append("<h3>").append(esc(c.getTitle()));
                if (!c.isFuture()) body.append(" <span class='muted'>(past)</span>");
                body.append("</h3>");
                body.append("<p>📅 ").append(c.getDateTime().format(DISPLAY_FMT))
                    .append(" &nbsp; 📍 ").append(esc(c.getLocation()))
                    .append(" &nbsp; 👥 ").append(c.getEnrolledStudents().size()).append("/").append(c.getCapacity())
                    .append("</p>");
                if (!c.getNotes().isEmpty()) body.append("<p class='muted'>").append(esc(c.getNotes())).append("</p>");

                body.append("<div><strong>Enrolled:</strong> ");
                if (c.getEnrolledStudents().isEmpty()) body.append("<span class='muted'>nobody yet</span>");
                else for (String name : c.getEnrolledStudents()) {
                    body.append("<span class='tag'>").append(esc(name))
                        .append(" <form method='post' action='/classes/").append(realIdx).append("/unenroll' style='display:inline'>")
                        .append("<input type='hidden' name='name' value='").append(esc(name)).append("'>")
                        .append("<button class='tag-x' type='submit'>×</button></form></span>");
                }
                body.append("</div>");

                if (!students.isEmpty() && c.isFuture()) {
                    body.append("<form method='post' action='/classes/").append(realIdx).append("/enroll' class='inline-form'>");
                    body.append("<select name='studentIdx'>");
                    for (int i = 0; i < students.size(); i++)
                        body.append("<option value='").append(i).append("'>").append(esc(students.get(i).getName())).append("</option>");
                    body.append("</select><button type='submit'>Enroll</button></form>");
                }
                body.append(formBtn("/classes/" + realIdx + "/delete", "Cancel class", "danger"));
                body.append("</div>");
            }
        }
        ctx.html(layout("Classes", body.toString()));
    }

    // ============ ACTIONS ============

    private static void addStudent(Context ctx) {
        String name = ctx.formParam("name");
        if (name != null && !name.isBlank()) {
            students.add(new Student(name.trim(),
                    nz(ctx.formParam("email")),
                    nz(ctx.formParam("phone")),
                    nz(ctx.formParam("membership")),
                    parseD(ctx.formParam("fee"))));
            save();
        }
        ctx.redirect("/students");
    }

    private static void studentAction(Context ctx, String action) {
        int i = Integer.parseInt(ctx.pathParam("idx"));
        if (i < 0 || i >= students.size()) return;
        Student s = students.get(i);
        switch (action) {
            case "attend" -> s.incrementAttendance();
            case "pay" -> s.setPaidThisMonth(true);
            case "unpay" -> s.setPaidThisMonth(false);
            case "delete" -> students.remove(i);
        }
        save();
    }

    private static void addClass(Context ctx) {
        String title = ctx.formParam("title");
        String dt = ctx.formParam("datetime");
        if (title == null || title.isBlank() || dt == null || dt.isBlank()) { ctx.redirect("/classes"); return; }
        try {
            LocalDateTime when = LocalDateTime.parse(dt, INPUT_FMT);
            classes.add(new YogaClass(title.trim(), when,
                    nz(ctx.formParam("location")),
                    (int) parseD(ctx.formParam("capacity")),
                    nz(ctx.formParam("notes"))));
            save();
        } catch (DateTimeParseException ignored) {}
        ctx.redirect("/classes");
    }

    private static void enroll(Context ctx) {
        int ci = Integer.parseInt(ctx.pathParam("idx"));
        int si = Integer.parseInt(ctx.formParamAsClass("studentIdx", Integer.class).get());
        if (ci >= 0 && ci < classes.size() && si >= 0 && si < students.size()) {
            classes.get(ci).enroll(students.get(si).getName());
            save();
        }
        ctx.redirect("/classes");
    }

    private static void unenroll(Context ctx) {
        int ci = Integer.parseInt(ctx.pathParam("idx"));
        String name = ctx.formParam("name");
        if (ci >= 0 && ci < classes.size() && name != null) {
            classes.get(ci).unenroll(name);
            save();
        }
        ctx.redirect("/classes");
    }

    // ============ HTML HELPERS ============

    private static String layout(String title, String body) {
        return """
            <!doctype html><html><head><meta charset='utf-8'>
            <title>%s — Yoga Studio Manager</title>
            <style>
              * { box-sizing: border-box; }
              body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                     margin: 0; background: #faf7f2; color: #2a2a2a; }
              nav { background: #4a6741; padding: 1rem 2rem; display: flex; gap: 1.5rem; align-items: center; }
              nav a { color: white; text-decoration: none; font-weight: 500; }
              nav a:hover { text-decoration: underline; }
              nav .brand { font-size: 1.3rem; font-weight: 700; margin-right: auto; }
              main { max-width: 1100px; margin: 0 auto; padding: 2rem; }
              h1 { margin-top: 0; }
              .card { background: white; border-radius: 12px; padding: 1.25rem 1.5rem;
                      margin: 1rem 0; box-shadow: 0 1px 3px rgba(0,0,0,.06); }
              .class-card.past { opacity: .6; }
              .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px,1fr)); gap: 1rem; }
              .stat { background: white; padding: 1.25rem; border-radius: 12px;
                      box-shadow: 0 1px 3px rgba(0,0,0,.06); }
              .stat .label { font-size: .85rem; color: #777; text-transform: uppercase; letter-spacing: .05em; }
              .stat .value { font-size: 1.6rem; font-weight: 700; margin-top: .25rem; color: #4a6741; }
              table { width: 100%%; border-collapse: collapse; background: white; border-radius: 12px; overflow: hidden;
                      box-shadow: 0 1px 3px rgba(0,0,0,.06); }
              th, td { padding: .75rem; text-align: left; border-bottom: 1px solid #eee; }
              th { background: #f0ebe3; font-weight: 600; }
              .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px,1fr)); gap: .75rem; align-items: end; }
              .form-grid label { display: flex; flex-direction: column; font-size: .85rem; color: #555; }
              input, select { padding: .5rem; border: 1px solid #ccc; border-radius: 6px; font-size: 1rem; margin-top: .25rem; }
              button { background: #4a6741; color: white; border: none; padding: .55rem 1rem;
                       border-radius: 6px; cursor: pointer; font-size: .9rem; }
              button:hover { background: #3a5331; }
              button.danger { background: #c0392b; }
              button.danger:hover { background: #962d22; }
              .actions { display: flex; gap: .3rem; flex-wrap: wrap; }
              .actions form { margin: 0; }
              .paid { color: #2e7d32; font-weight: 600; }
              .unpaid { color: #c62828; font-weight: 600; }
              .muted { color: #888; }
              .tag { display: inline-block; background: #e8e2d4; padding: .2rem .5rem; border-radius: 999px;
                     margin: .15rem; font-size: .85rem; }
              .tag-x { background: none; color: #c0392b; padding: 0 .25rem; font-size: 1rem; }
              .tag-x:hover { background: none; color: #962d22; }
              .inline-form { display: inline-flex; gap: .5rem; margin: .5rem .5rem .5rem 0; }
            </style></head><body>
            <nav>
              <span class='brand'>🧘 Yoga Studio</span>
              <a href='/'>Dashboard</a>
              <a href='/students'>Students</a>
              <a href='/classes'>Classes</a>
            </nav>
            <main>%s</main></body></html>
            """.formatted(esc(title), body);
    }

    private static String statCard(String label, String value) {
        return "<div class='stat'><div class='label'>" + esc(label) + "</div><div class='value'>" + esc(value) + "</div></div>";
    }

    private static String input(String name, String label, String type, boolean required) {
        return "<label>" + esc(label) + "<input type='" + type + "' name='" + name + "'" + (required ? " required" : "") + "></label>";
    }

    private static String formBtn(String action, String label, String cls) {
        return "<form method='post' action='" + action + "' style='display:inline'>"
                + "<button type='submit'" + (cls.isEmpty() ? "" : " class='" + cls + "'") + ">" + esc(label) + "</button></form>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String nz(String s) { return s == null ? "" : s.trim(); }
    private static double parseD(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0; } }

    // ============ PERSISTENCE ============

    @SuppressWarnings("unchecked")
    private static void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            students = (List<Student>) in.readObject();
            classes = (List<YogaClass>) in.readObject();
        } catch (Exception e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    private static void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(students);
            out.writeObject(classes);
        } catch (IOException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }
}
