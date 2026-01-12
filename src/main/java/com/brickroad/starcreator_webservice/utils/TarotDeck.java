package com.brickroad.starcreator_webservice.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TarotDeck {
    private final List<TarotCard> cards;
    private Random random;

    public TarotDeck() {
        cards = new ArrayList<>();
        random = new Random();
        initializeMajorArcana();
        initializeMinorArcana();
    }

    private void initializeMajorArcana() {
        cards.add(new TarotCard("0 - The Fool",
                "New beginnings, innocence, spontaneity, free spirit",
                "Recklessness, taken advantage of, inconsideration"));
        cards.add(new TarotCard("I - The Magician",
                "Manifestation, resourcefulness, power, inspired action",
                "Manipulation, poor planning, untapped talents"));
        cards.add(new TarotCard("II - The High Priestess",
                "Intuition, sacred knowledge, divine feminine, subconscious",
                "Secrets, disconnected from intuition, withdrawal"));
        cards.add(new TarotCard("III - The Empress",
                "Femininity, beauty, nature, abundance, nurturing",
                "Creative block, dependence on others, emptiness"));
        cards.add(new TarotCard("IV - The Emperor",
                "Authority, structure, control, fatherhood, establishment",
                "Domination, excessive control, rigidity, inflexibility"));
        cards.add(new TarotCard("V - The Hierophant",
                "Spiritual wisdom, tradition, conformity, institutions",
                "Rebellion, subversiveness, new approaches, challenging tradition"));
        cards.add(new TarotCard("VI - The Lovers",
                "Love, harmony, relationships, choices, alignment of values",
                "Self-love, disharmony, imbalance, misalignment of values"));
        cards.add(new TarotCard("VII - The Chariot",
                "Control, willpower, success, determination, direction",
                "Lack of control, lack of direction, aggression, opposition"));
        cards.add(new TarotCard("VIII - Strength",
                "Strength, courage, patience, compassion, self-control",
                "Inner strength, self-doubt, low energy, raw emotion"));
        cards.add(new TarotCard("IX - The Hermit",
                "Soul searching, introspection, inner guidance, solitude",
                "Isolation, loneliness, withdrawal, lost your way"));
        cards.add(new TarotCard("X - Wheel of Fortune",
                "Good luck, karma, life cycles, destiny, turning point",
                "Bad luck, resistance to change, breaking cycles"));
        cards.add(new TarotCard("XI - Justice",
                "Justice, fairness, truth, cause and effect, law",
                "Unfairness, lack of accountability, dishonesty"));
        cards.add(new TarotCard("XII - The Hanged Man",
                "Pause, surrender, letting go, new perspectives",
                "Delays, resistance, stalling, indecision"));
        cards.add(new TarotCard("XIII - Death",
                "Endings, change, transformation, transition",
                "Resistance to change, personal transformation, inner purging"));
        cards.add(new TarotCard("XIV - Temperance",
                "Balance, moderation, patience, purpose, meaning",
                "Imbalance, excess, self-healing, re-alignment"));
        cards.add(new TarotCard("XV - The Devil",
                "Shadow self, attachment, addiction, restriction, sexuality",
                "Releasing limiting beliefs, exploring dark thoughts, detachment"));
        cards.add(new TarotCard("XVI - The Tower",
                "Sudden change, upheaval, chaos, revelation, awakening",
                "Personal transformation, fear of change, averting disaster"));
        cards.add(new TarotCard("XVII - The Star",
                "Hope, faith, purpose, renewal, spirituality",
                "Lack of faith, despair, self-trust, disconnection"));
        cards.add(new TarotCard("XVIII - The Moon",
                "Illusion, fear, anxiety, subconscious, intuition",
                "Release of fear, repressed emotion, inner confusion"));
        cards.add(new TarotCard("XIX - The Sun",
                "Positivity, fun, warmth, success, vitality, joy",
                "Inner child, feeling down, overly optimistic"));
        cards.add(new TarotCard("XX - Judgement",
                "Reflection, reckoning, inner calling, absolution",
                "Self-doubt, inner critic, ignoring the call"));
        cards.add(new TarotCard("XXI - The World",
                "Completion, accomplishment, travel, fulfillment",
                "Seeking personal closure, short-cuts, delays"));
    }

    private void initializeMinorArcana() {
        // Cups (Emotion, relationships, feelings)
        initializeSuit("Cups", "Emotion, love, relationships, feelings");

        // Pentacles (Material, career, finances)
        initializeSuit("Pentacles", "Material, money, career, practical");

        // Swords (Intellect, thoughts, conflict)
        initializeSuit("Swords", "Intellect, thoughts, conflict, decision");

        // Wands (Energy, passion, action)
        initializeSuit("Wands", "Energy, passion, creativity, action");
    }

    private void initializeSuit(String suit, String theme) {
        String[][] meanings = getSuitMeanings(suit);

        cards.add(new TarotCard("Ace of " + suit, meanings[0][0], meanings[0][1]));
        cards.add(new TarotCard("Two of " + suit, meanings[1][0], meanings[1][1]));
        cards.add(new TarotCard("Three of " + suit, meanings[2][0], meanings[2][1]));
        cards.add(new TarotCard("Four of " + suit, meanings[3][0], meanings[3][1]));
        cards.add(new TarotCard("Five of " + suit, meanings[4][0], meanings[4][1]));
        cards.add(new TarotCard("Six of " + suit, meanings[5][0], meanings[5][1]));
        cards.add(new TarotCard("Seven of " + suit, meanings[6][0], meanings[6][1]));
        cards.add(new TarotCard("Eight of " + suit, meanings[7][0], meanings[7][1]));
        cards.add(new TarotCard("Nine of " + suit, meanings[8][0], meanings[8][1]));
        cards.add(new TarotCard("Ten of " + suit, meanings[9][0], meanings[9][1]));
        cards.add(new TarotCard("Page of " + suit, meanings[10][0], meanings[10][1]));
        cards.add(new TarotCard("Knight of " + suit, meanings[11][0], meanings[11][1]));
        cards.add(new TarotCard("Queen of " + suit, meanings[12][0], meanings[12][1]));
        cards.add(new TarotCard("King of " + suit, meanings[13][0], meanings[13][1]));
    }

    private String[][] getSuitMeanings(String suit) {
        return switch (suit) {
            case "Cups" -> new String[][]{
                    {"Love, new relationships, compassion, creativity", "Emotional loss, blocked creativity, emptiness"},
                    {"Unified love, partnership, mutual attraction", "Self-love, break-ups, disharmony, imbalance"},
                    {"Celebration, friendship, community, happiness", "Overindulgence, gossip, isolation"},
                    {"Meditation, contemplation, apathy, reevaluation", "Retreat, withdrawal, checking in for alignment"},
                    {"Regret, failure, disappointment, pessimism", "Personal setbacks, self-forgiveness, moving on"},
                    {"Revisiting the past, childhood memories, innocence", "Living in the past, forgiveness, lacking playfulness"},
                    {"Opportunities, choices, wishful thinking, illusion", "Alignment, personal values, overwhelmed by choices"},
                    {"Disappointment, abandonment, withdrawal, escapism", "Trying one more time, indecision, aimless drifting"},
                    {"Contentment, satisfaction, gratitude, wishes coming true", "Inner happiness, materialism, dissatisfaction"},
                    {"Divine love, blissful relationships, harmony, alignment", "Disconnection, misaligned values, struggling relationships"},
                    {"Creative opportunities, curiosity, possibility, messages", "New ideas, doubting intuition, creative blocks"},
                    {"Romantic, charming, idealistic, following the heart", "Moodiness, disappointment, unrealistic expectations"},
                    {"Compassionate, caring, emotionally stable, intuitive", "Insecurity, giving too much, self-care neglect"},
                    {"Emotionally balanced, compassionate, diplomatic", "Emotional manipulation, moodiness, volatility"}
            };
            case "Pentacles" -> new String[][]{
                    {"New financial opportunity, prosperity, manifestation", "Lost opportunity, lack of planning, poor prospects"},
                    {"Multiple priorities, time management, prioritization", "Over-committed, disorganization, reprioritization"},
                    {"Teamwork, collaboration, learning, implementation", "Disharmony, misalignment, working alone"},
                    {"Saving money, security, conservatism, scarcity", "Over-spending, greed, self-protection, insecurity"},
                    {"Financial loss, poverty, lack mindset, isolation", "Recovery, charity, improvement, spiritual wealth"},
                    {"Gifts, generosity, charity, sharing wealth", "Strings attached, inequality, extortion"},
                    {"Long-term view, sustainable results, perseverance", "Lack of rewards, impatience, frustration"},
                    {"Apprenticeship, education, quality, high standards", "Lack of focus, perfectionism, misdirected activity"},
                    {"Abundance, luxury, self-sufficiency, financial independence", "Self-worth, over-investment in work, hustling"},
                    {"Wealth, financial security, family, long-term success", "Financial failure, loneliness, loss"},
                    {"Manifestation, financial opportunity, skill development", "Lack of progress, procrastination, learn from failure"},
                    {"Efficiency, hard work, responsibility, routine", "Laziness, obsessiveness, work without reward"},
                    {"Nurturing, practical, providing, working parent", "Self-care, financial independence, work-home conflict"},
                    {"Wealth, business, leadership, security, discipline", "Financially inept, obsessed with wealth, stubborn"}
            };
            case "Swords" -> new String[][]{
                    {"Breakthroughs, new ideas, mental clarity, success", "Inner clarity, re-thinking an idea, clouded judgement"},
                    {"Difficult decisions, weighing options, stalemate", "Indecision, confusion, information overload"},
                    {"Heartbreak, emotional pain, sorrow, grief, hurt", "Recovery, forgiveness, moving on"},
                    {"Rest, restoration, contemplation, recuperation", "Exhaustion, burn-out, deep contemplation, stagnation"},
                    {"Conflict, disagreements, competition, defeat, win at all costs", "Reconciliation, making amends, past resentment"},
                    {"Transition, change, rite of passage, moving on", "Personal transition, resistance to change, unfinished business"},
                    {"Betrayal, deception, getting away with something, stealth", "Imposter syndrome, self-deceit, keeping secrets"},
                    {"Negative thoughts, self-imposed restriction, imprisonment", "Self-limiting beliefs, inner critic, releasing negative thoughts"},
                    {"Anxiety, worry, fear, depression, nightmares", "Inner turmoil, deep-seated fears, shame, releasing anxiety"},
                    {"Painful endings, deep wounds, betrayal, loss, crisis", "Recovery, regeneration, resisting an inevitable end"},
                    {"New ideas, curiosity, thirst for knowledge, new ways of thinking", "Self-expression, all talk no action, haphazard action"},
                    {"Ambitious, action-oriented, driven to succeed, fast-thinking", "Restless, unfocused, impulsive, burn-out"},
                    {"Independent, unbiased judgement, clear boundaries, direct", "Cold-hearted, cruel, bitterness, revenge"},
                    {"Mental clarity, intellectual power, authority, truth", "Quiet power, inner truth, misuse of power, manipulation"}
            };
            case "Wands" -> new String[][]{
                    {"Inspiration, new opportunities, growth, potential", "An emerging idea, lack of direction, distractions"},
                    {"Future planning, progress, decisions, discovery", "Personal goals, inner alignment, fear of unknown, lack of planning"},
                    {"Progress, expansion, foresight, overseas opportunities", "Playing small, lack of foresight, unexpected delays"},
                    {"Celebration, joy, harmony, relaxation, homecoming", "Personal celebration, inner harmony, conflict with others"},
                    {"Competition, conflict, rivalry, disagreements", "Inner conflict, conflict avoidance, tension release"},
                    {"Public recognition, progress, self-confidence, victory", "Private achievement, personal definition of success, fall from grace"},
                    {"Perseverance, defensive, maintaining control, challenges", "Exhaustion, giving up, overwhelmed"},
                    {"Movement, fast paced change, action, alignment, air travel", "Delays, frustration, resisting change, internal alignment"},
                    {"Resilience, courage, persistence, test of faith, boundaries", "Inner resources, struggle, overwhelm, defensive, paranoia"},
                    {"Burden, extra responsibility, hard work, completion", "Doing it all, carrying the burden, delegation, release"},
                    {"Inspiration, ideas, discovery, limitless potential, free spirit", "Newly-formed ideas, redirecting energy, self-limiting beliefs"},
                    {"Energy, passion, inspired action, adventure, impulsiveness", "Passion project, haste, scattered energy, delays"},
                    {"Courage, determination, joy, attraction, independence", "Self-respect, self-confidence, introverted, re-establish confidence"},
                    {"Natural-born leader, vision, entrepreneur, honour", "Impulsiveness, haste, ruthless, high expectations"}
            };
            default -> new String[14][2];
        };
    }

    public TarotCard drawCard(boolean allowReversed) {
        int index = random.nextInt(cards.size());
        TarotCard card = cards.get(index);
        cards.remove(index);

        if (allowReversed && random.nextBoolean()) {
            card.setReversed(true);
            card.setOrientation("Reversed");
            card.setMeaning(card.getReversedMeaning());
        } else {
            card.setOrientation("Upright");
            card.setMeaning(card.getUprightMeaning());
        }

        return card;
    }

    public List<TarotCard> drawMultipleCards(int count, boolean allowReversed) {
        List<TarotCard> drawn = new ArrayList<>();
        List<Integer> usedIndices = new ArrayList<>();

        for (int i = 0; i < count && i < cards.size(); i++) {
            int index;
            do {
                index = random.nextInt(cards.size());
            } while (usedIndices.contains(index));

            usedIndices.add(index);
            TarotCard card = cards.get(index);
            TarotCard drawnCard = new TarotCard(card.getName(), card.getUprightMeaning(), card.getReversedMeaning());

            if (allowReversed && random.nextBoolean()) {
                drawnCard.setReversed(true);
            }

            drawn.add(drawnCard);
        }

        return drawn;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public int getDeckSize() {
        return cards.size();
    }
}
