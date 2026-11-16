package com.nocturnal.ironman.ui;

import com.nocturnal.ironman.data.ActivityCatalog;
import com.nocturnal.ironman.data.BossCatalog;
import com.nocturnal.ironman.data.DestinationCatalog;
import com.nocturnal.ironman.data.SkillProgressionCatalog;
import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.Goal;
import com.nocturnal.ironman.service.AiService;
import com.nocturnal.ironman.service.PlannerService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.PluginPanel;

@SuppressWarnings({"serial", "this-escape"})
public class CompanionPanel extends PluginPanel
{
    private final PlannerService planner;
    private final AiService ai;
    private final ConfigManager configManager;
    private final ClientThread clientThread;
    private final JTextArea dashboard = area();
    private final JTextArea bank = area();
    private final JTextArea inventory = area();
    private final JTextArea activities = area();
    private final JTextArea quests = area();
    private final JTextArea farming = area();
    private final JTextArea slayer = area();
    private final JTextArea skills = area();
    private final JTextArea bossing = area();
    private final JComboBox<String> bossChoice = new JComboBox<>(BossCatalog.ALL.stream().map(b -> b.name).toArray(String[]::new));
    private final JComboBox<String> destination = new JComboBox<>(DestinationCatalog.ALL.stream().map(d -> d.name).toArray(String[]::new));
    private final JComboBox<String> activityChoice = new JComboBox<>(ActivityCatalog.ALL.stream().map(a -> a.title).toArray(String[]::new));
    private final JTextField questName = new JTextField(16);
    private final CardLayout sectionLayout = new CardLayout();
    private final JPanel sectionCards = new JPanel(sectionLayout);
    private final JComboBox<String> sectionChoice = new JComboBox<>();
    private final JTextArea navigation = area();
    private final JTextArea chat = area();
    private final JTextField bankFilter = new JTextField();
    private final JTextField inventoryFilter = new JTextField();
    private final JTextField prompt = new JTextField();
    private final JButton askButton = new JButton("Ask");
    private final JPanel goalsPanel = new JPanel(new BorderLayout());
    private final List<Goal> goals = new ArrayList<>();
    private final List<AiService.ChatMessage> aiHistory = new ArrayList<>();
    private long aiConversationRevision;
    private SwingWorker<String, Void> activeAiWorker;
    private AccountSnapshot snapshot;

    public CompanionPanel(PlannerService planner, AiService ai, ConfigManager configManager, ClientThread clientThread)
    {
        super(false);
        this.planner = planner;
        this.ai = ai;
        this.configManager = configManager;
        this.clientThread = clientThread;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel title = new JLabel("IRONMAN COMPANION"); title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JLabel subtitle = new JLabel("Your Ironman planning hub"); subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        JPanel header = new JPanel(new GridLayout(2,1)); header.add(title); header.add(subtitle); add(header, BorderLayout.NORTH);
        destination.setEditable(true);
        activityChoice.setEditable(true);
        bossChoice.setEditable(true);
        addSection("Overview", wrap(dashboard));
        addSection("Skills", skillsPanel());
        addSection("Bank", searchableItemsPanel(bank, bankFilter));
        addSection("Inventory", searchableItemsPanel(inventory, inventoryFilter));
        addSection("Goals", goalsPanel);
        addSection("Quests", questPanel());
        addSection("Farming", farmingPanel());
        addSection("Slayer", slayerPanel());
        addSection("Activities", activitiesPanel());
        addSection("Routes", routePanel());
        addSection("Bossing", bossingPanel());
        addSection("AI", aiPanel());

        JPanel navigationBar = new JPanel(new BorderLayout(6, 0));
        navigationBar.add(new JLabel("View"), BorderLayout.WEST);
        navigationBar.add(sectionChoice, BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.add(navigationBar, BorderLayout.NORTH);
        body.add(sectionCards, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        sectionChoice.addActionListener(e -> {
            Object selection = sectionChoice.getSelectedItem();
            if (selection != null) sectionLayout.show(sectionCards, selection.toString());
        });
        sectionChoice.setSelectedItem("Overview");
        renderGoals(); renderActivities(null);
        showEmpty();
    }

    private void addSection(String name, java.awt.Component content)
    {
        sectionChoice.addItem(name);
        sectionCards.add(content, name);
    }

    private JPanel routePanel()
    {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        JPanel selector = new JPanel(new BorderLayout(4, 0));
        selector.add(destination, BorderLayout.CENTER);
        JButton go = new JButton("Plan");
        selector.add(go, BorderLayout.EAST);
        go.addActionListener(e -> {
            if (snapshot == null) return;
            Object value = destination.getEditor().getItem();
            String target = value == null ? "" : value.toString().trim();
            if (!target.isEmpty()) navigation.setText(String.join("\n", planner.routeSuggestions(snapshot, target)));
        });
        JButton available = new JButton("My teleports");
        available.addActionListener(e -> navigation.setText("Available teleport items (bank snapshot included):\n\n" + String.join("\n", planner.teleports(snapshot))));
        JButton nearest = new JButton("Nearest");
        nearest.addActionListener(e -> navigation.setText("Nearest detected option:\n" + planner.nearestKnownTeleport(snapshot)));
        JButton herb = new JButton("Herb run");
        herb.addActionListener(e -> navigation.setText(String.join("\n", planner.herbRun(snapshot))));
        JPanel shortcuts = new JPanel(new GridLayout(2, 2, 4, 4));
        shortcuts.add(available);
        shortcuts.add(nearest);
        shortcuts.add(herb);
        JPanel controls = new JPanel(new BorderLayout(0, 5));
        controls.add(selector, BorderLayout.NORTH);
        controls.add(shortcuts, BorderLayout.CENTER);
        p.add(controls, BorderLayout.NORTH);
        p.add(wrap(navigation), BorderLayout.CENTER);
        return p;
    }

    private JPanel aiPanel()
    {
        JPanel p = new JPanel(new BorderLayout(4,4)); JPanel bottom = new JPanel(new BorderLayout(4,4));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        JButton clear = new JButton("Clear chat");
        clear.addActionListener(e -> clearAiConversation());
        actions.add(clear); actions.add(askButton);
        bottom.add(prompt, BorderLayout.NORTH); bottom.add(actions, BorderLayout.SOUTH);
        askButton.addActionListener(e -> askAi()); prompt.addActionListener(e -> askAi());
        p.add(wrap(chat), BorderLayout.CENTER); p.add(bottom, BorderLayout.SOUTH); return p;
    }

    private void askAi()
    {
        final String q = prompt.getText().trim();
        if (q.isEmpty()) return;
        final AccountSnapshot requestSnapshot = snapshot;
        final List<AiService.ChatMessage> requestHistory = new ArrayList<>(aiHistory);
        final long requestRevision = aiConversationRevision;
        prompt.setText("");
        chat.append("\nYou: " + q + "\nAssistant: thinking…\n");
        sendEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() { protected String doInBackground() throws Exception { return ai.ask(q, requestSnapshot, requestHistory); }
            protected void done()
            {
                if (activeAiWorker != this) return;
                activeAiWorker = null;
                sendEnabled(true);
                if (isCancelled()) return;
                try
                {
                    String answer = get();
                    if (requestRevision == aiConversationRevision)
                    {
                        aiHistory.add(new AiService.ChatMessage("user", q));
                        aiHistory.add(new AiService.ChatMessage("assistant", answer));
                        while (aiHistory.size() > 12) aiHistory.remove(0);
                        chat.append(answer + "\n\n");
                    }
                }
                catch (Exception ex)
                {
                    if (requestRevision == aiConversationRevision)
                        chat.append("Error: " + rootMessage(ex) + "\n\n");
                }
            }
        };
        activeAiWorker = worker;
        worker.execute();
    }
    private void sendEnabled(boolean enabled) { prompt.setEnabled(enabled); askButton.setEnabled(enabled); }
    private String rootMessage(Throwable t)
    {
        Throwable cause = t;
        while (cause instanceof java.util.concurrent.ExecutionException && cause.getCause() != null)
            cause = cause.getCause();
        if (cause instanceof IllegalStateException || cause instanceof IllegalArgumentException)
            return cause.getMessage() == null ? "The provider returned an error." : cause.getMessage();
        return "The request failed. Check your endpoint and network connection.";
    }

    private void clearAiConversation()
    {
        aiConversationRevision++;
        if (activeAiWorker != null) activeAiWorker.cancel(true);
        activeAiWorker = null;
        aiHistory.clear();
        chat.setText("");
        prompt.setText("");
        sendEnabled(true);
    }

    public void refresh(AccountSnapshot s)
    {
        if (s == null)
        {
            snapshot = null;
            clearAiConversation();
            goals.clear();
            renderGoals();
            showEmpty();
            return;
        }
        if (snapshot == null || !snapshot.getPlayerName().equals(s.getPlayerName())
            || !snapshot.getAccountType().equals(s.getAccountType()))
        {
            loadGoals();
            renderGoals();
            clearAiConversation();
        }
        if (snapshot != null
            && snapshot.getPlayerName().equals(s.getPlayerName())
            && snapshot.getRealLevels().equals(s.getRealLevels()) && snapshot.getXp().equals(s.getXp())
            && snapshot.getInventory().equals(s.getInventory()) && snapshot.getEquipment().equals(s.getEquipment())
            && snapshot.getBank().equals(s.getBank()) && snapshot.getLocation().equals(s.getLocation())
            && snapshot.getAccountType().equals(s.getAccountType())
            && snapshot.getSpellbook().equals(s.getSpellbook())
            && java.util.Objects.equals(snapshot.getBankCapturedAt(), s.getBankCapturedAt())
            && snapshot.isBankOpen() == s.isBankOpen()
            && snapshot.getProgress().getQuests().equals(s.getProgress().getQuests())
            && snapshot.getProgress().getDiaries().equals(s.getProgress().getDiaries())
            && sameSlayer(snapshot, s) && sameFarming(snapshot, s))
        {
            snapshot = s;
            return;
        }
        snapshot=s;
        if (s == null) { showEmpty(); return; }
        StringBuilder d=new StringBuilder();
        d.append("Player: ").append(s.getPlayerName()).append('\n');
        d.append("Account type: ").append(s.getAccountType()).append("   Spellbook: ").append(s.getSpellbook()).append('\n');
        d.append("Combat: ").append(s.getCombatLevel()).append("   Total level: ").append(s.getTotalLevel()).append('\n');
        d.append("Location: ").append(s.getLocation()).append("\n\nSKILLS\n");
        s.getRealLevels().forEach((k,v)->d.append(String.format("%-14s %3d   %,.0f XP%n",k,v,(double)s.getXp().getOrDefault(k,0))));
        d.append("\nPROGRESSION\nQuests: ").append(s.getProgress().getCompletedQuestCount()).append(" / ").append(s.getProgress().getQuestCount())
            .append(" completed · ").append(s.getProgress().getInProgressQuestCount()).append(" in progress\n");
        d.append("Achievement diaries: ").append(s.getProgress().getDiaries().size()).append(" regions tracked\n");
        if (s.getSlayerTask().isActive())
            d.append("Slayer: ").append(s.getSlayerTask().getRemaining()).append(" ").append(s.getSlayerTask().getTaskName()).append(" remaining\n");
        else d.append("Slayer: no active task detected\n");
        d.append("\n").append(String.join("\n", planner.progressionSuggestions(s))).append("\n\nACCOUNT SNAPSHOT\nInventory: ").append(s.getInventory().size()).append(" distinct items\nEquipment: ").append(s.getEquipment().size()).append(" distinct items\nCached bank: ").append(s.getBank().size()).append(" distinct items\n");
        d.append("Bank cache: ").append(bankStatus(s)).append("\n");
        d.append("\nBank data is a saved snapshot from the last time the bank interface was open; it is not a live remote bank query.\n");
        dashboard.setText(d.toString());
        inventory.setText(renderItems(s, s.getInventory(), inventoryFilter.getText()));
        bank.setText("BANK SNAPSHOT\n" + bankStatus(s) + "\n\n" + renderItems(s, s.getBank(), bankFilter.getText()));
        quests.setText(renderProgress(s));
        farming.setText(renderFarming(s));
        slayer.setText(renderSlayer(s));
        skills.setText(renderSkills(s));
        bossing.setText(String.join("\n", planner.bossReadiness(s, (String) bossChoice.getSelectedItem())));
        renderActivities(s);
    }

    private boolean sameFarming(AccountSnapshot a, AccountSnapshot b)
    {
        if (a.getFarmingPatches().size() != b.getFarmingPatches().size()) return false;
        Instant now = Instant.now();
        for (int i = 0; i < a.getFarmingPatches().size(); i++)
        {
            com.nocturnal.ironman.model.FarmingPatchSnapshot left = a.getFarmingPatches().get(i);
            com.nocturnal.ironman.model.FarmingPatchSnapshot right = b.getFarmingPatches().get(i);
            if (!left.getName().equals(right.getName()) || !left.getCrop().equals(right.getCrop())
                || !left.getState().equals(right.getState()) || left.getStage() != right.getStage()
                || !java.util.Objects.equals(left.getObservedAt(), right.getObservedAt())
                || !left.getStatusText(now).equals(right.getStatusText(now))) return false;
        }
        return true;
    }

    private boolean sameSlayer(AccountSnapshot a, AccountSnapshot b)
    {
        return a.getSlayerTask().isActive() == b.getSlayerTask().isActive()
            && a.getSlayerTask().getTaskName().equals(b.getSlayerTask().getTaskName())
            && a.getSlayerTask().getLocation().equals(b.getSlayerTask().getLocation())
            && a.getSlayerTask().getRemaining() == b.getSlayerTask().getRemaining()
            && a.getSlayerTask().getInitialAmount() == b.getSlayerTask().getInitialAmount()
            && a.getSlayerTask().getPoints() == b.getSlayerTask().getPoints()
            && a.getSlayerTask().getStreak() == b.getSlayerTask().getStreak();
    }

    private String bankStatus(AccountSnapshot s)
    {
        Instant captured = s.getBankCapturedAt();
        if (captured == null) return "Not captured yet — open your bank once.";
        Duration age = Duration.between(captured, Instant.now());
        String ageText;
        if (age.toMinutes() < 1) ageText = "just now";
        else if (age.toHours() < 1) ageText = age.toMinutes() + " minutes ago";
        else if (age.toDays() < 1) ageText = age.toHours() + " hours ago";
        else ageText = age.toDays() + " days ago";
        return (s.isBankOpen() ? "bank open · refreshed " : "saved snapshot · captured ") + ageText;
    }

    private String renderFarming(AccountSnapshot s)
    {
        StringBuilder b = new StringBuilder("HERB PATCH TRACKER\n");
        b.append("Patch states update when their regions are loaded. Growth times are estimates from the last observed stage.\n\n");
        for (com.nocturnal.ironman.model.FarmingPatchSnapshot patch : s.getFarmingPatches())
        {
            b.append(patch.getName()).append(" — ").append(patch.getCrop()).append('\n')
                .append("  ").append(patch.getStatusText(Instant.now())).append('\n');
        }
        return b.toString();
    }

    private String renderProgress(AccountSnapshot s)
    {
        StringBuilder b = new StringBuilder("QUEST PROGRESS\n\n");
        b.append("Completed: ").append(s.getProgress().getCompletedQuestCount()).append(" / ").append(s.getProgress().getQuestCount())
            .append(" · In progress: ").append(s.getProgress().getInProgressQuestCount()).append("\n");
        b.append("Recipe for Disaster subquests appear in the list but are excluded from the main quest count.\n\n");
        java.util.List<String> names = new java.util.ArrayList<>(s.getProgress().getQuests().keySet());
        names.sort(String.CASE_INSENSITIVE_ORDER);
        for (String name : names)
        {
            String state = s.getProgress().getQuests().get(name);
            String label = "FINISHED".equals(state) ? "Complete" : "IN_PROGRESS".equals(state) ? "In progress" : "Not started";
            b.append(String.format("%-44s %s%n", name, label));
        }
        b.append("\n\nACHIEVEMENT DIARIES\n\n");
        s.getProgress().getDiaries().forEach((name, status) -> b.append(name).append('\n').append(status).append("\n\n"));
        b.append("Quest states are read from the RuneLite quest API. Diary tiers are read from the corresponding game varbits.");
        return b.toString();
    }

    private String renderSkills(AccountSnapshot s)
    {
        StringBuilder b = new StringBuilder("SKILL PROGRESSION\n\n");
        b.append(String.format("%-15s %5s %13s %13s%n", "Skill", "Level", "XP", "XP to 99"));
        b.append("---------------------------------------------------------------\n");
        for (java.util.Map.Entry<String, Integer> entry : s.getRealLevels().entrySet())
        {
            int level = entry.getValue();
            int xp = s.getXp().getOrDefault(entry.getKey(), 0);
            int to99 = Math.max(0, SkillProgressionCatalog.xpForLevel(99) - xp);
            b.append(String.format("%-15s %5d %,13d %,13d%n", entry.getKey(), level, xp, to99));
        }
        b.append("\nNEXT MILESTONES\n\n");
        b.append(String.join("\n", planner.progressionSuggestions(s)));
        b.append("\n\nXP to level uses the OSRS experience curve. Milestones are account-planning checkpoints; consult the AI/Wiki tab for a full Ironman training route and current method comparisons.");
        return b.toString();
    }

    private String renderSlayer(AccountSnapshot s)
    {
        com.nocturnal.ironman.model.SlayerTaskSnapshot task = s.getSlayerTask();
        StringBuilder b = new StringBuilder("SLAYER TRACKER\n\n");
        b.append("Points: ").append(task.getPoints()).append("\nTask streak: ").append(task.getStreak()).append("\n\n");
        if (!task.isActive()) return b.append("No active Slayer assignment detected.").toString();
        b.append("Task: ").append(task.getTaskName()).append('\n');
        b.append("Remaining: ").append(task.getRemaining()).append(" / ").append(task.getInitialAmount()).append('\n');
        b.append("Completed: ").append(task.getCompleted()).append(" ( ").append(task.getProgressPercent()).append("% )\n");
        b.append("Area: ").append(task.getLocation()).append("\n\n");
        b.append("Use the AI tab to request a task-specific gear, inventory and supply checklist. Verify the task area and current mechanics against the Wiki.");
        return b.toString();
    }

    private JPanel searchableItemsPanel(JTextArea area, JTextField filter)
    {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        JPanel search = new JPanel(new BorderLayout(4, 0));
        search.add(new JLabel("Search"), BorderLayout.WEST);
        search.add(filter, BorderLayout.CENTER);
        filter.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override public void insertUpdate(DocumentEvent e) { updateItemFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { updateItemFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { updateItemFilters(); }
        });
        panel.add(search, BorderLayout.NORTH);
        panel.add(wrap(area), BorderLayout.CENTER);
        return panel;
    }

    private void updateItemFilters()
    {
        AccountSnapshot current = snapshot;
        if (current == null) return;
        inventory.setText(renderItems(current, current.getInventory(), inventoryFilter.getText()));
        bank.setText("BANK SNAPSHOT\n" + bankStatus(current) + "\n\n"
            + renderItems(current, current.getBank(), bankFilter.getText()));
    }

    private String renderItems(AccountSnapshot account, java.util.Map<Integer,Integer> items, String filter)
    {
        String query = filter == null ? "" : filter.trim().toLowerCase(java.util.Locale.ROOT);
        StringBuilder b = new StringBuilder();
        int matches = 0;
        long totalQuantity = 0;
        for (java.util.Map.Entry<Integer, Integer> entry : items.entrySet())
        {
            String name = account.getItemName(entry.getKey());
            if (!query.isEmpty() && !name.toLowerCase(java.util.Locale.ROOT).contains(query)
                && !Integer.toString(entry.getKey()).contains(query)) continue;
            matches++;
            totalQuantity += entry.getValue();
            b.append(String.format("%-36s x %,d  [id %d]%n", name, entry.getValue(), entry.getKey()));
        }
        if (items.isEmpty()) return "No cached items. Open the relevant container in-game to capture it.";
        if (matches == 0) return "No items match \"" + filter + "\".\n" + items.size() + " distinct item IDs in this snapshot.";
        return "Showing " + matches + " of " + items.size() + " distinct item IDs · "
            + String.format("%,d", totalQuantity) + " total quantity\n\n" + b;
    }

    private void renderActivities(AccountSnapshot s)
    {
        StringBuilder b = new StringBuilder("PLANNING LIBRARY\n\n");
        for (ActivityCatalog.Activity a : ActivityCatalog.ALL)
            b.append(a.category).append(" — ").append(a.title).append('\n').append(a.summary).append('\n')
                .append("Requirements: ").append(a.requirements).append('\n')
                .append("Preparation: ").append(a.supplies).append("\n\n");
        if (s != null)
        {
            b.append("\n\nCURRENT ACCOUNT — HERB RUN ROUTE\n\n");
            b.append(String.join("\n", planner.herbRun(s)));
        }
        else b.append("\nOpen the plugin while logged in to generate a bank-aware herb-run route.");
        b.append("\n\nThese are planning templates, not a complete verified database of every OSRS activity. Confirm exact current requirements against the OSRS Wiki.");
        activities.setText(b.toString());
    }

    private JPanel questPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel controls = new JPanel(new BorderLayout(0, 5));
        JButton questGuide = new JButton("Plan this quest");
        JButton nextUnlock = new JButton("Find my next unlock");
        questGuide.addActionListener(e -> {
            String quest = questName.getText().trim();
            if (!quest.isEmpty()) queueAiPrompt("Build a complete Ironman quest guide for " + quest +
                ". Use my current quest states, skill levels, diaries, inventory, equipment, bank and teleports. List prerequisites, boostable skill checks, exact item/supply checklist, recommended inventory, travel route, safe alternatives and permanent account unlocks. Cite current OSRS Wiki pages and distinguish hard requirements from recommendations.");
        });
        nextUnlock.addActionListener(e -> queueAiPrompt("Analyse my current Ironman quest and diary progress, levels, bank and teleports. Rank the next 10 highest-value quest and diary unlocks for account progression. For each, explain why it matters, prerequisites, required levels/items, likely time/cost and the best order to complete them. Cite current OSRS Wiki pages and do not assume I have banked items available without withdrawing them."));
        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 4));
        actions.add(questGuide);
        actions.add(nextUnlock);
        controls.add(questName, BorderLayout.NORTH);
        controls.add(actions, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(quests), BorderLayout.CENTER);
        return panel;
    }

    private JPanel activitiesPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel controls = new JPanel(new BorderLayout(0, 5));
        JButton plan = new JButton("Build account-aware plan");
        plan.addActionListener(e -> {
            Object value = activityChoice.getEditor().getItem();
            String activity = value == null ? "" : value.toString().trim();
            if (!activity.isEmpty()) queueAiPrompt("Build a practical Ironman plan for " + activity +
                " using my current skills, quests, diaries, inventory, equipment, bank, Slayer assignment and teleports. Cover unlocks, preparation, supplies, best Ironman methods, route and transport, alternatives, progression value and what I should do next. Cite current OSRS Wiki sources. Clearly label any requirement or mechanic you cannot verify.");
        });
        controls.add(activityChoice, BorderLayout.NORTH);
        controls.add(plan, BorderLayout.SOUTH);
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(activities), BorderLayout.CENTER);
        return panel;
    }

    private void queueAiPrompt(String value)
    {
        prompt.setText(value);
        sectionChoice.setSelectedItem("AI");
        prompt.requestFocusInWindow();
    }

    private JPanel farmingPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel controls = new JPanel(new GridLayout(2, 1, 0, 5));
        JButton route = new JButton("Build accessible herb route");
        route.addActionListener(e -> {
            if (snapshot == null)
            {
                farming.setText("Log in to build an account-aware herb route.");
                return;
            }
            farming.setText(renderFarming(snapshot) + "\n\n" + String.join("\n", planner.herbRun(snapshot)));
        });
        JButton aiPlan = new JButton("Draft optimized herb strategy");
        aiPlan.addActionListener(e -> {
            queueAiPrompt("Build an account-aware OSRS Ironman herb strategy. Use my actual Farming level, unlocked herb patches, observed patch states and growth estimates, quest and diary requirements, current spellbook, carried/equipped teleports, banked transport, fairy-ring access, Quetzal sites and available supplies. Recommend the best herb seed I can plant, compost and disease protection, which patches to visit now versus defer, a low-travel route, and the next unlocks that would improve future runs. Separate confirmed access from unknown or conditional unlocks, and cite current OSRS Wiki sources.");
        });
        controls.add(route);
        controls.add(aiPlan);
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(farming), BorderLayout.CENTER);
        return panel;
    }

    private JPanel slayerPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton taskGuide = new JButton("Draft Slayer task guide");
        taskGuide.addActionListener(e -> {
            if (snapshot == null || !snapshot.getSlayerTask().isActive())
            {
                slayer.setText("No active Slayer assignment detected. Log in or wait for the task snapshot to refresh.");
                return;
            }
            String task = snapshot.getSlayerTask().getTaskName();
            queueAiPrompt("Build a complete Ironman Slayer guide for my current task: " + task + ". Use my actual Slayer level, combat stats, quests, diaries, spellbook, equipment and bank. Recommend the best eligible monster locations, task-specific boss options, gear and weapon alternatives, supplies, cannon/burst/barrage options where applicable, safe spots, teleport routes and unlocks. Distinguish task eligibility from optional upgrades, and cite current OSRS Wiki sources. Do not assume I own items that are only in the bank unless you identify them as banked.");
        });
        controls.add(taskGuide);
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(slayer), BorderLayout.CENTER);
        return panel;
    }

    private JPanel skillsPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton progression = new JButton("Draft progression plan");
        progression.addActionListener(e -> {
            queueAiPrompt("Build a detailed Ironman progression plan for my current OSRS account. Use my real skill levels, XP, quest and diary progress, active spellbook, equipment and bank. Prioritise account unlocks, efficient training methods, useful quest prerequisites, transport unlocks, skilling supplies and boss readiness. Cover every skill, but rank the next 5 highest-value goals rather than giving generic advice. Use current OSRS Wiki sources and cite them. Separate hard requirements, recommendations and account-specific assumptions.");
        });
        controls.add(progression);
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(skills), BorderLayout.CENTER);
        return panel;
    }

    private JPanel bossingPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel controls = new JPanel(new BorderLayout(0, 5));
        JButton readiness = new JButton("Check readiness");
        JButton aiGuide = new JButton("Draft full AI guide");
        readiness.addActionListener(e -> {
            Object value = bossChoice.getEditor().getItem();
            String boss = value == null ? "" : value.toString().trim();
            bossing.setText(snapshot == null ? "Log in to check boss readiness."
                : String.join("\n", planner.bossReadiness(snapshot, boss)));
        });
        aiGuide.addActionListener(e -> {
            Object value = bossChoice.getEditor().getItem();
            String boss = value == null ? "" : value.toString().trim();
            if (!boss.isEmpty()) queueAiPrompt("Build a complete Ironman-specific guide for " + boss +
                " using my current account, equipment and bank. Verify current access requirements, recommended stats, Ironman-obtainable gear alternatives, inventory and supply quantities, teleport routes, mechanics, death risks and progression value. Use current OSRS Wiki sources, cite relevant pages, and separate hard requirements from recommendations. If this boss has variants or multiple methods, compare them.");
        });
        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 4));
        actions.add(readiness);
        actions.add(aiGuide);
        controls.add(bossChoice, BorderLayout.NORTH);
        controls.add(actions, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(bossing), BorderLayout.CENTER);
        bossing.setText("Select a boss and check your quest, Slayer, skill, teleport and preparation readiness.\n\nThe readiness view is a starting checklist; use Draft full AI guide for detailed mechanics and Ironman gear alternatives.");
        return panel;
    }

    private void renderGoals()
    {
        goalsPanel.removeAll(); JPanel list=new JPanel(); list.setLayout(new javax.swing.BoxLayout(list, javax.swing.BoxLayout.Y_AXIS));
        for (Goal g:goals)
        {
            JCheckBox check=new JCheckBox(g.getTitle()+"  ["+g.getCategory()+"]",g.isComplete());
            check.addActionListener(e->{g.setComplete(check.isSelected()); saveGoals();});
            JLabel detail=new JLabel("  "+g.getDetail()+(g.getTargetDate()==null?"":" · target "+g.getTargetDate()));
            JPanel row=new JPanel(new BorderLayout()); row.add(check,BorderLayout.NORTH); row.add(detail,BorderLayout.CENTER); row.setBorder(BorderFactory.createEmptyBorder(4,2,4,2)); list.add(row);
        }
        JPanel controls=new JPanel(new GridLayout(1, 2, 4, 0)); JButton add=new JButton("Add goal"); JButton remove=new JButton("Remove done");
        add.addActionListener(e->addGoal()); remove.addActionListener(e->{goals.removeIf(Goal::isComplete);saveGoals();renderGoals();}); controls.add(add);controls.add(remove);
        goalsPanel.add(controls,BorderLayout.NORTH);goalsPanel.add(new JScrollPane(list),BorderLayout.CENTER);goalsPanel.revalidate();goalsPanel.repaint();
    }

    private void addGoal()
    {
        JTextField title=new JTextField(); JTextField category=new JTextField("General"); JTextField detail=new JTextField();
        JComboBox<String> date=new JComboBox<>(new String[]{"No date","7 days","30 days","90 days"});
        Object[] fields={"Goal",title,"Category",category,"Notes / target",detail,"Target",date};
        if(JOptionPane.showConfirmDialog(this,fields,"Add Ironman goal",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        if(title.getText().trim().isEmpty())return;
        LocalDate target=null; if(date.getSelectedIndex()>0)target=LocalDate.now().plusDays(date.getSelectedIndex()==1?7:date.getSelectedIndex()==2?30:90);
        goals.add(new Goal(title.getText().trim(),category.getText().trim(),detail.getText().trim(),target));saveGoals();renderGoals();
    }

    private void loadGoals()
    {
        goals.clear();
        String raw = configManager.getRSProfileConfiguration("ironmancompanion", "goals");
        if (raw == null || raw.isEmpty()) return;
        try
        {
            for (String line : raw.split("\n"))
            {
                String[] p = line.split("\\|", -1);
                if (p.length != 6) continue;
                Goal goal = new Goal(dec(p[0]), dec(p[1]), dec(p[2]), p[3].isEmpty() ? null : LocalDate.parse(p[3]));
                goal.setComplete(Boolean.parseBoolean(p[4]));
                goals.add(goal);
            }
        }
        catch (Exception ignored)
        {
            goals.clear();
        }
    }

    private void saveGoals()
    {
        StringBuilder b = new StringBuilder();
        for (Goal g : goals)
            b.append(enc(g.getTitle())).append('|').append(enc(g.getCategory())).append('|')
                .append(enc(g.getDetail())).append('|').append(g.getTargetDate() == null ? "" : g.getTargetDate())
                .append('|').append(g.isComplete()).append("|x\n");
        String payload = b.toString();
        clientThread.invokeLater(() -> configManager.setRSProfileConfiguration("ironmancompanion", "goals", payload));
    }
    private String enc(String s){return Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    private String dec(String s){return new String(Base64.getDecoder().decode(s),java.nio.charset.StandardCharsets.UTF_8);}
    private void showEmpty()
    {
        dashboard.setText("Log into Old School RuneScape to capture your account.\n\nOpen your bank once to populate the saved bank snapshot.\n\nUse the View selector to review inventory, quests, Slayer, goals, activities, routes and AI planning.");
        bank.setText("Open your bank to capture its contents.");
        inventory.setText("Log in to capture inventory.");
        quests.setText("Log in to capture quest and achievement diary progress.");
        farming.setText("Log in to track herb patch states.");
        slayer.setText("Log in to detect your current Slayer assignment.");
        skills.setText("Log in to calculate XP and skill milestones.");
        bossing.setText("Log in to check boss readiness.");
        navigation.setText("Log in to calculate routes and available teleports.");
        renderActivities(null);
    }
    private JTextArea area(){JTextArea a=new JTextArea();a.setEditable(false);a.setLineWrap(true);a.setWrapStyleWord(true);a.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));return a;}
    private JScrollPane wrap(JTextArea a){return new JScrollPane(a);}
}
