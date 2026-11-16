package com.nocturnal.ironman.data;

import com.nocturnal.ironman.model.AccountSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.coords.WorldPoint;

/** Existing permanent spirit trees. Player-grown trees require separate build-state evidence. */
public final class SpiritTreeCatalog
{
    private SpiritTreeCatalog() {}

    public static final class Tree
    {
        public final String name;
        public final WorldPoint location;
        private final String accessQuest;

        private Tree(String name, int x, int y, String accessQuest)
        {
            this.name = name;
            location = new WorldPoint(x, y, 0);
            this.accessQuest = accessQuest;
        }

        public boolean canDepart(AccountSnapshot account)
        {
            return !"Gnome Stronghold".equals(name)
                || completed(account, "The Grand Tree");
        }
    }

    public static final List<Tree> ALL = Collections.unmodifiableList(Arrays.asList(
        new Tree("Tree Gnome Village", 2539, 3166, null),
        new Tree("Gnome Stronghold", 2460, 3446, null),
        new Tree("Battlefield of Khazard", 2555, 3259, null),
        new Tree("Grand Exchange", 3185, 3510, null),
        new Tree("Feldip Hills", 2486, 2850, null),
        new Tree("Incomitatus", 2339, 3111, "The Path of Glouphrie"),
        new Tree("Prifddinas", 3275, 6124, "Song of the Elves")
    ));

    public static List<Tree> available(AccountSnapshot account)
    {
        if (!completed(account, "Tree Gnome Village")) return Collections.emptyList();
        List<Tree> trees = new ArrayList<>();
        for (Tree tree : ALL)
            if (tree.accessQuest == null || completed(account, tree.accessQuest)) trees.add(tree);
        return Collections.unmodifiableList(trees);
    }

    private static boolean completed(AccountSnapshot account, String name)
    {
        return account != null
            && "FINISHED".equals(account.getProgress().getQuests().get(name));
    }
}
