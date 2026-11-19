package dating.cli;

import dating.exception.IllegalActionException;
import dating.matching.Matcher;
import dating.matching.Recommender;
import dating.model.NetworkView;
import dating.model.Photo;
import dating.runner.Simulation;
import dating.sim.Command;
import dating.sim.SimulationEngine;
import dating.stats.Statistics;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Interactive mode: a human controls one profile from the console, the other profiles are
 * played by bots, which make one round of moves after every successful human action.
 */
final class PlaySession {
    private final SimulationEngine engine;
    private final Simulation bots;
    private final int me;
    private final Scanner in;
    private final PrintStream out;
    private final ConsoleRenderer renderer = new ConsoleRenderer();

    PlaySession(SimulationEngine engine, Simulation bots, int me, Scanner in, PrintStream out) {
        this.engine = engine;
        this.bots = bots;
        this.me = me;
        this.in = in;
        this.out = out;
    }

    void run() {
        NetworkView n = engine.network();
        Matcher matcher = new Matcher(n);
        Recommender recommender = new Recommender(n);
        out.println("You are " + renderer.render(n.profile(me)));
        out.println("Type 'help' for the list of commands.");
        while (true) {
            out.print("> ");
            if (!in.hasNextLine()) {
                return;
            }
            String[] words = in.nextLine().trim().split("\\s+");
            String cmd = words[0].toLowerCase();
            try {
                switch (cmd) {
                    case "" -> { }
                    case "help" -> help();
                    case "quit", "exit" -> {
                        return;
                    }
                    case "me" -> out.println(renderer.render(n.profile(me)));
                    case "match" -> matcher.rank(me,
                                    dating.filter.ProfileFilters.compatibleWith(n.profile(me)).and(matcher.notFriendsWith(me)),
                                    number(words, 1, 5))
                            .forEach(r -> out.println(renderer.render(r)));
                    case "know" -> recommender.mightKnow(me, number(words, 1, 5))
                            .forEach(r -> out.println(renderer.render(r)));
                    case "photos" -> n.photosOf(number(words, 1, me)).stream().map(Photo::id)
                            .forEach(id -> out.println("photo " + id + ", likes: " + n.likers(id).size()));
                    case "requests" -> n.incoming(me).forEach(r -> out.println("from user " + r.from()));
                    case "friends" -> out.println(n.friends().neighbors(me));
                    case "stats" -> {
                        out.println("likes received by user: " + Statistics.mostLikedProfiles(n, 5));
                        out.println("profiles by city: " + Statistics.profilesByCity(n));
                    }
                    case "like" -> act(new Command.LikePhoto(me, number(words, 1, -1)));
                    case "request" -> act(new Command.SendFriendRequest(me, number(words, 1, -1)));
                    case "accept" -> act(new Command.AcceptFriendRequest(me, number(words, 1, -1)));
                    case "decline" -> act(new Command.DeclineFriendRequest(me, number(words, 1, -1)));
                    case "unfriend" -> act(new Command.RemoveFriend(me, number(words, 1, -1)));
                    case "interest" -> act(new Command.AddInterest(me, rest(words)));
                    case "city" -> act(new Command.ChangeCity(me, rest(words)));
                    case "pass" -> bots.playRound();
                    default -> out.println("Unknown command. Type 'help'.");
                }
            } catch (IllegalActionException | IllegalArgumentException ex) {
                out.println("Not allowed: " + ex.getMessage());
            }
        }
    }

    private void act(Command command) {
        engine.apply(command);
        bots.playRound();
    }

    private static int number(String[] words, int index, int fallback) {
        if (words.length > index) {
            return Integer.parseInt(words[index]);
        }
        if (fallback < 0) {
            throw new IllegalArgumentException("a number is required");
        }
        return fallback;
    }

    private static String rest(String[] words) {
        if (words.length < 2) {
            throw new IllegalArgumentException("an argument is required");
        }
        return String.join(" ", java.util.Arrays.copyOfRange(words, 1, words.length));
    }

    private void help() {
        out.println("me | friends | requests | photos [userId] | stats");
        out.println("match [n]        best candidates for you");
        out.println("know [n]         people you may know");
        out.println("like <photoId> | request <userId> | accept <userId> | decline <userId> | unfriend <userId>");
        out.println("interest <name> | city <name> | pass (let bots move) | quit");
    }
}
