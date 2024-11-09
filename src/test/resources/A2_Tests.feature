Feature: A2 Tests
  # Scenario 1
  Scenario: A1_scenario
    Given a new game
    And a deck rigged for scenario 1
    When P1 draws a quest of 4 stages
    Then P1 refuses to sponsor
    And P2 agrees to sponsor
    And P2 is the sponsor
    And P2 builds stage 1 with [F5 Horse]
    And P2 builds stage 2 with [F15 Sword]
    And P2 builds stage 3 with [F15 Dagger Battle-axe]
    And P2 builds stage 4 with [F40 Battle-axe]
    And stage 1 of the quest begins
    And P1 decides to participate in the stage, drawing F30 and trimming F5
    And P3 decides to participate in the stage, drawing Sword and trimming F5
    And P4 decides to participate in the stage, drawing Battle-axe and trimming F5
    And P1 attacks with [Dagger Sword]
    And P3 attacks with [Sword Dagger]
    And P4 attacks with [Dagger Horse]
    And P1 wins the stage
    And P3 wins the stage
    And P4 wins the stage
    And stage 2 of the quest begins
    And P1 decides to participate in the stage, drawing F10
    And P3 decides to participate in the stage, drawing Lance
    And P4 decides to participate in the stage, drawing Lance
    And P1 attacks with [Horse Sword]
    And P3 attacks with [Battle-axe Sword]
    And P4 attacks with [Horse Battle-axe]
    And P1 loses the stage
    And P3 wins the stage
    And P4 wins the stage
    And stage 3 of the quest begins
    And P3 decides to participate in the stage, drawing Battle-axe
    And P4 decides to participate in the stage, drawing Sword
    And P3 attacks with [Lance Horse Sword]
    And P4 attacks with [Battle-axe Sword Lance]
    And P3 wins the stage
    And P4 wins the stage
    And stage 4 of the quest begins
    And P3 decides to participate in the stage, drawing F30
    And P4 decides to participate in the stage, drawing Lance
    And P3 attacks with [Battle-axe Horse Lance]
    And P4 attacks with [Dagger Sword Lance Excalibur]
    And P3 loses the stage
    And P4 wins the stage
    And the quest is finished
    And the sponsor updates their hand, drawing [F5 F5 F5 F10 F15 F20 F40 F70 D5 D5 S10 H10 L20] and trimming [F5 F5 F10 F70]
    And P1 has 0 shields
    And P2 has 0 shields
    And P3 has 0 shields
    And P4 has 4 shields
    And P1 has 9 cards
    And P2 has 12 cards
    And P3 has 5 cards
    And P4 has 4 cards

#  # Scenario 2
#  Scenario: 2winner_game_2winner_quest
#    Given a new game
#    And a rigged deck for scenario 1
#    # add
#    Then P2 has 7 shields
#    And P4 has 7 shields
#    And P2 won the game
#    And P4 won the game

  # Scenario 3
  Scenario: 1winner_game_with_events
    Given a new game
    And a deck rigged for scenario 3
    When P1 draws a quest of 4 stages
    Then P1 agrees to sponsor
    And P1 is the sponsor
    And P1 builds stage 1 with [F5 Horse]
    And P1 builds stage 2 with [F15 Sword]
    And P1 builds stage 3 with [F15 Dagger Battle-axe]
    And P1 builds stage 4 with [F40]
    And stage 1 of the quest begins
    And P2 decides to participate in the stage, drawing F30 and trimming F5
    And P3 decides to participate in the stage, drawing Sword and trimming F5
    And P4 decides to participate in the stage, drawing Battle-axe and trimming F5
    And P2 attacks with [Dagger Sword]
    And P3 attacks with [Sword Dagger]
    And P4 attacks with [Dagger Horse]
    And P2 wins the stage
    And P3 wins the stage
    And P4 wins the stage
    And stage 2 of the quest begins
    And P2 decides to participate in the stage, drawing F10
    And P3 decides to participate in the stage, drawing Lance
    And P4 decides to participate in the stage, drawing Lance
    And P2 attacks with [Horse Battle-axe]
    And P3 attacks with [Battle-axe Sword]
    And P4 attacks with [Horse Battle-axe]
    And P2 wins the stage
    And P3 wins the stage
    And P4 wins the stage
    And stage 3 of the quest begins
    And P2 decides to participate in the stage, drawing Lance
    And P3 decides to participate in the stage, drawing Battle-axe
    And P4 decides to participate in the stage, drawing Sword
    And P2 attacks with [Lance Battle-axe]
    And P3 attacks with [Lance Horse Sword]
    And P4 attacks with [Battle-axe Sword Lance]
    And P2 wins the stage
    And P3 wins the stage
    And P4 wins the stage
    And stage 4 of the quest begins
    And P2 decides to participate in the stage, drawing F5
    And P3 decides to participate in the stage, drawing F30
    And P4 decides to participate in the stage, drawing Lance
    And P2 attacks with [Horse Lance Battle-axe]
    And P3 attacks with [Battle-axe Horse Lance]
    And P4 attacks with [Dagger Sword Lance Excalibur]
    And P2 wins the stage
    And P3 wins the stage
    And P4 wins the stage
    And the quest is finished
    And the sponsor updates their hand, drawing [F5 F5 F5 F10 F15 F20 F40 F70 D5 D5 S10 H10] and trimming [F5 F5 F10 F70]
    And P1 has 0 shields
    And P2 has 4 shields
    And P3 has 4 shields
    And P4 has 4 shields
    And P1 has 12 cards
    And P2 has 6 cards
    And P3 has 5 cards
    And P4 has 4 cards
    And P2 draws the event Plague
    And the event runs
    And P1 has 0 shields
    And P2 has 2 shields
    And P3 has 4 shields
    And P4 has 4 shields
    And P3 draws the event Prosperity
    And the event runs, causing some players to trim their hands:
      | player | trimming    |
      | P1     | F40 S10 E30 |
    And P1 has 12 cards
    And P2 has 8 cards
    And P3 has 7 cards
    And P4 has 6 cards
    And P4 draws the event Queen's Favor
    And the event runs
    And P1 has 12 cards
    And P2 has 8 cards
    And P3 has 7 cards
    And P4 has 8 cards
    And P1 draws a quest of 3 stages
    And P1 agrees to sponsor
    And P1 is the sponsor
    And P1 builds stage 1 with [F5]
    And P1 builds stage 2 with [F5 D5]
    And P1 builds stage 3 with [F15]
    And stage 1 of the quest begins
    And P2 decides to participate in the stage, drawing D5
    And P3 decides to participate in the stage, drawing D5
    And P4 decides to participate in the stage, drawing D5
    And P2 attacks with [D5]
    And P3 attacks with [D5]
    And P4 attacks with []
    And P2 wins the stage
    And P3 wins the stage
    And P4 loses the stage
    And stage 2 of the quest begins
    And P2 decides to participate in the stage, drawing S10
    And P3 decides to participate in the stage, drawing H10
    And P2 attacks with [S10]
    And P3 attacks with [H10]
    And P2 wins the stage
    And P3 wins the stage
    And stage 3 of the quest begins
    And P2 decides to participate in the stage, drawing B15
    And P3 decides to participate in the stage, drawing B15
    And P2 attacks with [B15]
    And P3 attacks with [B15]
    And P2 wins the stage
    And P3 wins the stage
    And the quest is finished
    And the sponsor updates their hand, drawing [F5 F10 F15 F20 D5 H10 L20] and trimming [F5 F10 F15]
    And P1 has 0 shields
    And P2 has 5 shields
    And P3 has 7 shields
    And P4 has 4 shields
    And P1 did not win the game
    And P2 did not win the game
    And P3 won the game
    And P4 did not win the game

  # Scenario 4
  Scenario: 0_winner_quest
    Given a new game
    And a deck rigged for scenario 4
    When P1 draws a quest of 2 stages
    Then P1 agrees to sponsor
    And P1 is the sponsor
    And P1 builds stage 1 with [F15 Battle-axe]
    And P1 builds stage 2 with [F40 Sword Lance Horse]
    And stage 1 of the quest begins
    And P2 decides to participate in the stage, drawing Sword
    And P3 decides to participate in the stage, drawing Battle-axe
    And P4 decides to participate in the stage, drawing Lance
    And P2 attacks with [Sword]
    And P3 attacks with [Battle-axe]
    And P4 attacks with [Lance]
    And P2 loses the stage
    And P3 loses the stage
    And P4 loses the stage
    And the quest is finished
    And the sponsor updates their hand, drawing [F5 F10 F15 F20 F40 F70 D5 D5] and trimming []
    And P1 has 0 shields
    And P2 has 0 shields
    And P3 has 0 shields
    And P4 has 0 shields
    And P1 has 8 cards
    And P2 has 0 cards
    And P3 has 0 cards
    And P4 has 0 cards
