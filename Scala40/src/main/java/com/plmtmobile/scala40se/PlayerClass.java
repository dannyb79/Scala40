package com.plmtmobile.scala40se;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by daniele on 20/10/13.
 */
public class PlayerClass implements Serializable {

    public class SelectedCard implements Serializable {
        public int         value;
        SelectedCard ( int value ) {
            this.value       = value;
        }
    }

    public static class PlayerCard implements Serializable {
        public  int         value;
        public  boolean     selected;

        PlayerCard ( int value, boolean selected ) {
            this.value       = value;
            this.selected    = selected;
        }
    }

    /*
        definizioni pubbliche
    */
    public static final int     MAX_PLAYER_CARDS        = 14;     // n. di carte massimo in mano a un giocatore
    public static final int     GAME_START_CARDS        = 13;     // n. di carte distribuite al giocatore a inizo partita
    public static final int     OPENING_SCORE           = 40;     // punteggio minimo x apertura

    public static final int     PLAYER_ID               = 0;        // identificativo giocatore
    public static final int     DEVICE_ID               = 1;        // identificativo giocatore avversario (dispositivo)
    public static final int     MASTERPLAYER_ID         = 2;        // identificativo giocatore master (modalita' bluetooth)
    public static final int     RIVALPLAYER_ID          = 3;        // identificativo giocatore slave (modalita' bluetooth)
    public static final int     UNDEFINEDPLAYER_ID      = 10;       // identificativo giocatore non definito

    public static final int     PICKED_FROM_UNKNOWN     = 0;        // origine carta pescata non impostata
    public static final int     PICKED_FROM_POOL        = 1;        // carta pescata dal tallone
    public static final int     PICKED_FROM_CULL        = 2;        // carta pescata dal pozzo

    /*
        variabili giocatore
    */
    public  boolean             has_picked;                     // il giocatore ha pescato
    public  boolean             has_opened;                     // il giocatore ha aperto
    private boolean             opening_turn;                   // apertura nel turno corrente
    public  int                 turn_score;                     // punteggio nel turno corrente x verifica apertura
    private int                 player_id;                      // id giocatore (PLAYER_ID o DEVICE_ID)
    public  int                 losing_score;                   // punteggio calcolato in caso di perdita a fine partita
    public  int                 picked_from;                    // origine della carta

    ArrayList<PlayerCard>       cards           = new ArrayList<PlayerCard>();
    ArrayList<SelectedCard>     selectedCards   = new ArrayList<SelectedCard>();

    public int                  card_picked;                    // carta pescata per iniziare il turno
    ArrayList<PlayerCard>       start_cards     = new ArrayList<PlayerCard>();

    PlayerClass( int player_id ) {
        this.has_picked     = false;
        this.has_opened     = false;
        this.opening_turn   = false;
        this.turn_score     = 0;
        this.losing_score   = 0;
        this.picked_from    = PICKED_FROM_UNKNOWN;
        this.player_id      = player_id;
        this.cards.clear();
        this.selectedCards.clear();
    }

    public void reset( DeckClass deck ) {
        has_picked      = false;
        has_opened      = false;
        opening_turn    = false;
        turn_score      = 0;
        losing_score    = 0;
        picked_from     = PICKED_FROM_UNKNOWN;
        cards.clear();
        for( int i = 0; i < GAME_START_CARDS; i++ ) {
            cards.add( new PlayerCard( deck.pick_card(), false ) );
        }
        selectedCards.clear();
        sort_cards_straights();
    }

    public void start_turn() {
        has_picked      = false;
        turn_score      = 0;
        opening_turn    = false;
        // backup delle carte iniziali (non e' inclusa la carta pescata)
        start_cards.clear();
        for( int i = 0; i < cards.size(); i++ ) {
            start_cards.add( new PlayerCard( cards.get( i ).value, false ) );
        }
        picked_from     = PICKED_FROM_UNKNOWN;
    }

    public void start_restore( boolean add_picked_card ) {
        opening_turn    = false;
        cards.clear();
        for( int i = 0; i < start_cards.size(); i++ ) {
            cards.add( new PlayerCard( start_cards.get( i ).value, false ) );
        }
        if( add_picked_card ) {
            cards.add( new PlayerCard( card_picked, false ) );
        }
    }

    public void set_player_id( int new_player_id ) {
        this.player_id = new_player_id;
    }

    public void sort_cards_straights() {
        Collections.sort( cards, new CardsStraightsComparator() );
    }

    public void sort_cards_values() {
        Collections.sort( cards, new CardsValuesComparator() );
    }

    public void sort_cards_straights_reverse() {
        Collections.sort( cards, new CardsStraightsReverseComparator() );
    }

    public void sort_cards_values_reverse() {
        Collections.sort( cards, new CardsValuesReverseComparator() );
    }

    public void pick_from_pool( DeckClass deck ) {
        card_picked = deck.pick_card();
        cards.add( new PlayerCard( card_picked, false ) );
        has_picked  = true;
        picked_from = PICKED_FROM_POOL;
    }

    public boolean select_card( int position ) {
        if( ( position >= 0 ) && ( position < MAX_PLAYER_CARDS ) ) {
            // modifica lo stato di selezione della carta
            cards.get( position ).selected = !cards.get( position ).selected;

            //Log.d( Debug.debugTag, "Selezione carta in posizione " + Integer.toString( position) + " : " + Boolean.toString( cards.get( position ).selected ) );

            // se la carta ora e' selezionata...
            if( cards.get( position ).selected ) {
                // ...viene aggiunta alla lista di carte selezioante
                selectedCards.add( new SelectedCard( cards.get( position ).value ) );
            } else {
                // altrimenti viene rimossa dalla lista di carte selezionate
                for( int i = 0; i < selectedCards.size(); i++ ) {
                    if( selectedCards.get( i ).value == cards.get( position ).value ) {
                        selectedCards.remove( i );
                        break;
                    }
                }
            }
        }
        return cards.get( position ).selected;
    }


    public CullActionResult cull_action( DeckClass deck, ArrayList<GroupClass> groups ) {
        boolean car_update_screen       = false;
        boolean car_discard_card        = false;
        boolean car_open_failure        = false;
        boolean car_cull_pick_failure   = false;

        // se il giocatore ha gia' pescato
        if( has_picked ) {
            // verifico le condizioni dello scarto (1 carta sola)
            if( selectedCards.size() == 1 ) {
                // se il giocatore ha aperto
                if( has_opened ) {
                    // se il giocatore ha aperto nel turno corrente pescando dal pozzo devo verificare che la carta
                    // pescata sia presente nelle combinazioni calate sul tavolo
                    if( ( opening_turn ) && ( picked_from == PICKED_FROM_CULL ) ) {
                        // cerco la carta pescata nelle combinazioni calate
                        boolean picked_in_opening_cards = false;
                        for( int i = 0; i < groups.size(); i++ ) {
                            if( groups.get( i ).owner == player_id ) {
                                for( int k = 0; k < groups.get( i ).total_cards; k++ )  {
                                    if( groups.get( i ).cards[ k ] == card_picked ) {
                                        picked_in_opening_cards = true;
                                        break;
                                    }
                                }
                                if( picked_in_opening_cards ) {
                                    break;
                                }
                            }
                        }
                        // se la carta pescata dal pozzo e' tra quelle calate per aprire lo scarto e' permesso!
                        if( picked_in_opening_cards ) {
                            // scarto OK
                            // aggiunge la carta selezionata al pozzo
                            deck.add_cull( selectedCards.get( 0 ).value );
                            // rimuove la carta selezionata dalle carte in mano al giocatore
                            for( int i = 0; i < cards.size(); i++ ) {
                                if( cards.get( i ).selected ) {
                                    cards.remove( i );
                                    break;
                                }
                            }
                            // annulla qualunque selezione
                            selectedCards.clear();

                            car_discard_card    = true;
                            car_update_screen   = true;
                        } else {
                            // altriemnti riporta la situazione di gioco a inizio turno
                            // azzera il punteggio del turno
                            turn_score      = 0;
                            // azzera il flag "carta pescata"
                            has_picked      = false;
                            // azzera il flag "apertura"
                            has_opened      = false;
                            // ripristina le carte del giocatore a inizio turno SENZA la carta pescata
                            start_restore( false );
                            // rimette la carta pescata nel pozzo
                            deck.add_cull( card_picked );
                            // rimozione gruppi sul tavolo appartenenti al giocatore
                            for( int i = ( groups.size() - 1 ); i >= 0; i-- ) {
                                if( groups.get( i ).owner == player_id /*PLAYER_ID*/ ) {
                                    groups.remove( i );
                                }
                            }
                            // azzera la selezione di qualuque carta
                            selectedCards.clear();

                            car_cull_pick_failure   = true;
                            car_update_screen       = true;
                        }

                    } else {
                        // scarto OK
                        // aggiunge la carta selezionata al pozzo
                        deck.add_cull( selectedCards.get( 0 ).value );
                        // rimuove la carta selezionata dalle carte in mano al giocatore
                        for( int i = 0; i < cards.size(); i++ ) {
                            if( cards.get( i ).selected ) {
                                cards.remove( i );
                                break;
                            }
                        }
                        // annulla qualunque selezione
                        selectedCards.clear();

                        car_discard_card    = true;
                        car_update_screen   = true;
                    }
                } else {
                    // se il giocatore non ha ancora aperto e ha pescato dal tallone
                    if( picked_from == PICKED_FROM_POOL ) {
                        // se tenta di aprire con meno di 40 punti deve essere annullato lo scarto
                        if( ( turn_score > 0 ) && ( turn_score < OPENING_SCORE ) ) {
                            // apertura fallita: tentativo di scarto con meno di 40 punti
                            // ripristino le condizioni di inizio turno:
                            turn_score = 0;
                            // carte giocatore: carte iniziali + carta pescata
                            start_restore( true );
                            // rimozione gruppi sul tavolo appartenenti al giocatore
                            for( int i = ( groups.size() - 1 ); i >= 0; i-- ) {
                                if( groups.get( i ).owner == player_id /*PLAYER_ID*/ ) {
                                    groups.remove( i );
                                }
                            }
                            selectedCards.clear();

                            car_open_failure    = true;
                            car_update_screen   = true;

                        } else {
                            // (punti >= 40) scarto OK
                            // aggiunge la carta selezionata al pozzo
                            deck.add_cull( selectedCards.get( 0 ).value );
                            // rimuove la carta selezionata dalle carte in mano al giocatore
                            for( int i = 0; i < cards.size(); i++ ) {
                                if( cards.get( i ).selected ) {
                                    cards.remove( i );
                                    break;
                                }
                            }
                            // annulla qualunque selezione
                            selectedCards.clear();

                            car_discard_card    = true;
                            car_update_screen   = true;
                        }
                    } else {
                        // se il giocatore non ha ancora aperto e ha pescato dal pozzo secondo la variante a
                        // dell'ART.9 delle regole della f.i.sca la carta pescata deve essere utilizzata obbligatoriamente
                        // per aprire altrimenti vengono annullate le operazioni e riportato tutto a inizio turno

                        // se il giocatore non ha aperto lo scarto viene annullato e viene riportato tutto a inizio turno
                        if( turn_score < OPENING_SCORE ) {
                            // riporta la situazione di gioco a inizio turno
                            // azzera il punteggio del turno
                            turn_score      = 0;
                            // azzera il flag "carta pescata"
                            has_picked      = false;
                            // ripristina le carte del giocatore a inizio turno SENZA la carta pescata
                            start_restore( false );
                            // rimette la carta pescata nel pozzo
                            deck.add_cull( card_picked );
                            // rimozione gruppi sul tavolo appartenenti al giocatore
                            for( int i = ( groups.size() - 1 ); i >= 0; i-- ) {
                                if( groups.get( i ).owner == player_id /*PLAYER_ID*/ ) {
                                    groups.remove( i );
                                }
                            }
                            // azzera la selezione di qualuque carta
                            selectedCards.clear();

                            car_cull_pick_failure   = true;
                            car_update_screen       = true;
                        } else {
                            // il caso in cui il giocatore apre pescando dal pozzo e' gestito sopra!
                        }
                    }
                }
            }
        } else {
            // se sceglie di pescare dal pozzo
            has_picked  = true;
            picked_from = PICKED_FROM_CULL;
            card_picked = deck.pick_from_cull();
            cards.add( new PlayerCard( card_picked, false ) );

            car_update_screen = true;
        }
        return new CullActionResult( car_update_screen, car_discard_card, car_open_failure, car_cull_pick_failure );
    }

    public void remove_selected_from_player_cards() {
        for( int i = ( cards.size() - 1 ); i >= 0; i-- ) {
            if( cards.get( i ).selected == true ) {
                cards.remove( i );
            }
        }
    }

    public void reset_cards_selection() {
        for( int i = 0; i < cards.size(); i++  ) {
            cards.get( i ).selected = false;
        }
    }


    public TableActionResult table_action( int selected_group, ArrayList<GroupClass> groups ) {

        int         tmpcards[]          = new int[ PlayerClass.MAX_PLAYER_CARDS * 2 ], i;
        int         tmpjokercard        = 0;
        boolean     tar_update_screen   = false;
        boolean     tar_opening         = false;
        boolean     tar_score_update    = false;
        boolean     tar_cannot_discard  = false;
        int         error               = 0;

        if( ( selectedCards.size() - cards.size() ) == 0 ) {
            tar_cannot_discard = true;
        } else {
            // se l'indice vale -1 e' stata selezionata una posizione vuota..
            if( selected_group == Graphics.EMPTY_TABLE_CARD ) {
                // verifica se le carte selezionate dal giocatore costituiscono una combinazione valida
                for( i = 0; i < selectedCards.size(); i++ ) {
                    tmpcards[ i ] = selectedCards.get( i ).value;
                }
                EvaluationResult eval = evaluate_cards( selectedCards.size(), tmpcards );
                if( eval.is_valid ) {
                    // aggiorno il punteggio e verifico apertura
                    if( has_opened == false ) {
                        turn_score += eval.score;
                        tar_score_update = true;
                        if( turn_score >= OPENING_SCORE ) {
                            has_opened      = true;
                            opening_turn    = true;
                            tar_opening     = true;
                        }
                    }

                    remove_selected_from_player_cards();
                    selectedCards.clear();
                    groups.add( new GroupClass( player_id, eval.total_cards, eval.cards, eval.type, eval.joker_value, eval.score ) );
                    tar_update_screen = true;
                } else {
                    error = Errors.ADDGROUP_INVALID_COMB;
                }

            } else {
                // se il giocatore non ha ancora aperto, in fase di apertura puo' sostituire i joker
                // o attaccare solo nelle proprie combinazioni
                boolean groups_activities = true;
                if ( has_opened == false ) {
                    if( groups.get( selected_group ).owner != player_id ) {
                        groups_activities = false;
                    }
                }
                // controllo abilitazione del giocatore ad attaccare o sostituire i jolly
                if( groups_activities ) {
                    // controllo condizioni per sostituzione joker
                    if( ( selectedCards.size() == 1 )                               &&  // 1 sola carta selezionata
                            ( selectedCards.get( 0 ).value < DeckClass.BLACK_JOKER )    &&  // la carta selezionata non deve essere un jolly
                            ( groups.get( selected_group ).joker_value >= 0 )           &&  // il gruppo scelto deve gia' contenere un jolly
                            ( ( selectedCards.get( 0 ).value % 13 ) == groups.get( selected_group ).joker_value )   // la carta selezionata e' del valore nominale del jolly
                            ) {
                        // sostituzione jolly e valutazione combinazione
                        for( i = 0; i < groups.get( selected_group ).total_cards; i++ ) {
                            if( groups.get( selected_group ).cards[ i ] >= DeckClass.BLACK_JOKER ) {
                                tmpjokercard  = groups.get( selected_group ).cards[ i ];
                                tmpcards[ i ] = selectedCards.get( 0 ).value;
                            } else {
                                tmpcards[ i ] = groups.get( selected_group ).cards[ i ];
                            }
                        }
                        EvaluationResult eval = evaluate_cards( groups.get( selected_group ).total_cards, tmpcards );
                        if( eval.is_valid ) {
                            // swap tra carta selezionata dal giocatore e joker contenuto nella combinazione
                            for( i = 0; i < cards.size(); i++ ) {
                                if( cards.get( i ).selected ) {
                                    cards.get( i ).value = tmpjokercard;
                                }
                            }
                            selectedCards.clear();
                            reset_cards_selection();
                            groups.get( selected_group ).modify( eval.total_cards, eval.cards, eval.type, eval.joker_value, eval.score );
                            tar_update_screen = true;
                        } else {
                            error = Errors.JOKERSUB_INVALID_COMB;
                        }
                    } else {
                        // attacco
                        // creazione di un gruppo di carte equivalente alla somma delle carte gia' presenti nel gruppo
                        // + le carte selezionate dal giocatore
                        for( i = 0; i < groups.get( selected_group ).total_cards; i++ ) {
                            tmpcards[ i ] = groups.get( selected_group ).cards[ i ];
                        }
                        for( i = 0; i < selectedCards.size(); i++ ) {
                            tmpcards[ i + groups.get( selected_group ).total_cards ] = selectedCards.get( i ).value;
                        }
                        int group_old_score = 0;
                        if( has_opened == false ) {
                            group_old_score = groups.get( selected_group ).score;
                        }
                        EvaluationResult eval = evaluate_cards( groups.get( selected_group ).total_cards + selectedCards.size(), tmpcards );
                        if( eval.is_valid ) {
                            // se il giocatore non ha ancora aperto aggiorno il punteggio e verifico se apre
                            if( has_opened == false ) {
                                turn_score -= group_old_score;
                                turn_score += eval.score;
                                tar_score_update = true;
                                if( turn_score >= OPENING_SCORE ) {
                                    has_opened      = true;
                                    opening_turn    = true;
                                    tar_opening     = true;
                                }
                            }

                            remove_selected_from_player_cards();
                            selectedCards.clear();
                            // aggiunta delle carte alle carte appartenenti al gruppo gia' esistente
                            groups.get( selected_group ).modify( eval.total_cards, eval.cards, eval.type, eval.joker_value, eval.score );
                            tar_update_screen = true;
                        } else {
                            error = Errors.ATTACHMT_INVALID_COMB;
                        }
                    }
                }
            }
        }
        return new TableActionResult( tar_update_screen, tar_opening, tar_score_update, tar_cannot_discard, error );
    }


    private boolean is_valid_combination( int card_count[], int suit_count[], int joker_count, int tmp_total_cards ) {
        boolean possible_comb = true;
        int     i;

        if( tmp_total_cards <= 4 ) {
            for ( i = 0; i < 4; i++ )
                if( suit_count[ i ] > 1 ) {
                    possible_comb = false;
                    break;
                }
            if( possible_comb ) {
                for ( i = 0; i < 13; i++ ) {
                    if( ( card_count[ i ] > 0 ) && ( ( card_count[ i ] + joker_count ) == tmp_total_cards ) ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int get_combination_score( int tmp_total_cards, int tmp_cards[] ) {
        int score = 0, card_value = 0;
        for( int i = 0; i < tmp_total_cards; i++ ) {
            if( tmp_cards[ i ] < DeckClass.BLACK_JOKER ) {
                card_value = tmp_cards[ i ] % 13;
                break;
            }
        }
        if( card_value == 0 ) {
            score = tmp_total_cards * 11; // l'Asso vale 11 se messo in un tris (Art. 1.1)
        } else if ( card_value >= 10 ) {
            score = tmp_total_cards * 10; // le figure ( indice 10 (J), 11 (Q) e 12 (K) valgono 10 punti (Art. 1.1)
        } else if ( ( card_value >= 1 ) && ( card_value <= 9 ) ) {
            score = tmp_total_cards * ( card_value + 1 ); // il valore delle carte dal 2 al 10 e' quello nominale (Art. 1.1)
        }
        return score;
    }

    private int get_combination_joker_value( int joker_count, int tmp_total_cards, int tmp_cards[] ) {
        int joker_value = -1;
        if( joker_count > 0 ) {
            for( int i = 0; i < tmp_total_cards; i++ ) {
                if( tmp_cards[ i ] < DeckClass.BLACK_JOKER ) {
                    joker_value = tmp_cards[ i ] % 13;
                    break;
                }
            }
        }
        return joker_value;
    }

    private boolean is_valid_straight( int card_count[], int suit_count[], int joker_count, int tmp_total_cards, int tmp_cards[], boolean ace_flag, int highest_card, int lowest_card ) {
        boolean     joker_free;
        int         joker_pos;
        int         straight_suit = 0;
        int         k = 0, i;

        // verifico se e' possibile una scala valida (tutte le carte dello stesso seme)
        for ( i = 0; i < 4; i++ )
            if( suit_count[ i ] > 0 ) {
                k += 1;
                straight_suit = i;
            }
        if( k != 1 ) return false;
        // per essere una scala valida ogni valore deve avere solo un'occorrenza
        k = 0;
        for ( i = 0; i < 13; i++ ) {
            if( card_count[ i ] > 1 ) {
                k = 1;
                break;
            }
        }
        if( k > 0 ) return false;
        // il n. massimo di carte in una scala e' 14
        if( tmp_total_cards > 14 ) return false;

        // se non c'e' il joker
        if( joker_count == 0 ) {
            // .. e nemmeno l'asso
            if( ace_flag == false ) {
                // ... e' facile :)
                // devono esserci un un n. di carte pari all'intervallo tra la carta piu' bassa e quella piu' alta
                k  = ( highest_card - lowest_card );
                // se l'intervallo coincide con il n. di carte...
                if( k == ( tmp_total_cards - 1 ) ) {
                    // ... e' una scala valida, ordino l'array
                    Arrays.sort( tmp_cards, 0, tmp_total_cards );
                    return true;
                }
            } else {
                // se c'e' l'asso :/ ...
                // ricerca consecutivi in avanti A-2-3-4... fino al K (12) incluso, NON OLTRE!!!
                i = 1;
                while( ( i < 13 ) && ( card_count[ i ] > 0 ) ) {
                    i += 1;
                }
                if( i == tmp_total_cards ) {
                    // ... e' una scala valida, ordino l'array
                    Arrays.sort( tmp_cards, 0, tmp_total_cards );
                    return true;
                }
                // ricerca consecutivi all'indietro A-K-Q-J...fino al 2 (1) incluso, NON OLTRE!!!
                k = 1;
                i = 12; // K
                while( ( i > 0 ) && ( card_count[ i ] > 0 ) ) {
                    i -= 1;
                    k += 1;
                }
                if( k == tmp_total_cards ) {
                    // ... e' una scala valida, ordino l'array
                    Arrays.sort( tmp_cards, 0, tmp_total_cards );
                    // ma devo spostare l'asso (che si trova nella prima posizione) nell'ultima posizione
                    int ace_card = tmp_cards[ 0 ];
                    for( i = 0; i < ( tmp_total_cards - 1 ); i++ ) {
                        tmp_cards[ i ] = tmp_cards[ i + 1 ];
                    }
                    tmp_cards[ tmp_total_cards - 1 ] = ace_card;
                    return true;
                }
            }
        } else {
// verifica scale che includono un joker
            if( ace_flag == true ) {

                // sono presenti l'asso e il joker :/ i casi possono essere 2:
                // Asso - [max 12 carte consecutive] - Joker (ricerca in avanti)
                // Joker - [max 12 carte consecutive] - Asso (ricerca all'indietro)

                // ricerca in avanti A - carte - joker

                // ordino l'array in modo che iL joker si trovi sempre nell'ultima posizione
                boolean forward_search = true;
                Arrays.sort( tmp_cards, 0, tmp_total_cards );
                joker_free  = true;
                joker_pos   = -1;
                // scorrimento array fino alla terzultima carta (escluse l'ultima e il joker)
                for( i = 0; i < ( tmp_total_cards - 2 ); i++ ) {
                    int diff = tmp_cards[ i + 1 ] - tmp_cards[ i ];
                    // diff = 0 significa che sono presenti 2 carte uguali !!!
                    // diff > 2 significa che il salto di valore impedisce qualunque scala
                    // poiche' non risolvibile nemmeno con il joker (es: 4c -> 7c)
                    if( ( diff == 0 ) || ( diff > 2 ) ) {
                        forward_search = false;
                        break;
                    }
                    if( diff == 2 ) {
                        if( joker_free ) {
                            joker_free  = false;
                            joker_pos   = i + 1;
                        } else {
                            forward_search = false;
                            break;
                        }
                    }
                }
                // se la ricerca in avanti e' terminata con successo...
                if( forward_search ) {
                    // controllo se il joker deve essere utilizzato per sostituire una carta all'interno della scala...
                    if( joker_pos >= 0 ) {
                        // swap tra la carta che si trova nella posizione in cui dovrebbe trovarsi la carta mancante e il joker
                        int tmp_card = tmp_cards[ joker_pos ];
                        tmp_cards[ joker_pos ] = tmp_cards[ tmp_total_cards - 1 ];
                        tmp_cards[ tmp_total_cards - 1 ] = tmp_card;
                        // riordinamento array
                        Arrays.sort( tmp_cards, joker_pos + 1, tmp_total_cards );
                    }
                    return true;
                }

                // ricerca all'indietro
                // ordina l'array per avere:
                // - Asso come prima carta
                // - carte comprese dal 2 al K in ordine ascendente
                // - Joker come ultima carta
                // carte in ordine ascendente: Asso - [ carte ] - Joker
                Arrays.sort( tmp_cards, 0, tmp_total_cards );
                // inversione ordine carte centrali, esempio:
                // Ac Jc Qc Kc Joker -> Ac Kc Qc Jc Joker
                int searchcards[] = new int[ PlayerClass.MAX_PLAYER_CARDS ];
                for( i = 1; i < ( tmp_total_cards - 1 ); i++ ) {
                    searchcards[ i ] = tmp_cards[ tmp_total_cards - ( 1 + i ) ];
                }
                // joker
                searchcards[ tmp_total_cards - 1 ] = tmp_cards[ tmp_total_cards - 1 ];
                // indice K per il seme della scala + 1
                searchcards[ 0 ] = ( ( straight_suit * 13 ) + 12 ) + 1;

                joker_free  = true;
                joker_pos   = -1;
                // scorrimento array fino alla terzultima carta (escluse l'ultima e il joker)
                for( i = 0; i < ( tmp_total_cards - 2 ); i++ ) {
                    int diff = searchcards[ i ] - searchcards[ i + 1 ];
                    // diff = 0 significa che sono presenti 2 carte uguali !!!
                    // diff > 2 significa che il salto di valore impedisce qualunque scala
                    // poiche' non risolvibile nemmeno con il joker (es: 4c -> 7c)
                    if( ( diff == 0 ) || ( diff > 2 ) ) {
                        return false;
                    }
                    if( diff == 2 ) {
                        if( joker_free ) {
                            joker_free  = false;
                            joker_pos   = i + 1;
                        } else {
                            return false;
                        }
                    }
                }
                // se il joker deve essere utilizzato per sostituire una carta all'interno della scala...
                if( joker_pos >= 0 ) {
                    // swap tra la carta che si trova nella posizione in cui dovrebbe trovarsi la carta mancante e il joker
                    int tmp_card = searchcards[ joker_pos ];
                    searchcards[ joker_pos ] = searchcards[ tmp_total_cards - 1 ];
                    searchcards[ tmp_total_cards - 1 ] = tmp_card;
                    // riordinamento array
                    Arrays.sort( searchcards, joker_pos + 1, tmp_total_cards );
                    int[] reversesort = new int[ PlayerClass.MAX_PLAYER_CARDS ];
                    for( i = 0; i < ( tmp_total_cards - ( joker_pos + 1 ) ); i++ ) {
                        reversesort[ i ] = searchcards[ tmp_total_cards - 1 - i ];
                    }
                    for( i = 0; i < ( tmp_total_cards - ( joker_pos + 1 ) ); i++ ) {
                        searchcards[ joker_pos + 1 + i ] = reversesort[ i ];
                    }
                }
                //searchcards[ 0 ] -= 13;
                searchcards[ 0 ] = tmp_cards[ 0 ];
                for( i = 0; i < tmp_total_cards; i++ ) {
                    tmp_cards[ i ] = searchcards[ ( tmp_total_cards - 1 ) - i ];
                }
                return true;
            } else {
                // ordino l'array in modo che iL joker si trovi sempre nell'ultima posizione
                Arrays.sort( tmp_cards, 0, tmp_total_cards );
                joker_free  = true;
                joker_pos   = -1;
                // scorrimento array fino alla terzultima carta (escluse l'ultima e il joker)
                for( i = 0; i < ( tmp_total_cards - 2 ); i++ ) {
                    int diff = tmp_cards[ i + 1 ] - tmp_cards[ i ];
                    // diff = 0 significa che sono presenti 2 carte uguali !!!
                    // diff > 2 significa che il salto di valore impedisce qualunque scala
                    // poiche' non risolvibile nemmeno con il joker (es: 4c -> 7c)
                    if( ( diff == 0 ) || ( diff > 2 ) ) {
                        return false;
                    }
                    if( diff == 2 ) {
                        if( joker_free ) {
                            joker_free  = false;
                            joker_pos   = i + 1;
                        } else {
                            return false;
                        }
                    }
                }
                // se il joker deve essere utilizzato per sostituire una carta all'interno della scala...
                if( joker_pos >= 0 ) {
                    // swap tra la carta che si trova nella posizione in cui dovrebbe trovarsi la carta mancante e il joker
                    int tmp_card = tmp_cards[ joker_pos ];
                    tmp_cards[ joker_pos ] = tmp_cards[ tmp_total_cards - 1 ];
                    tmp_cards[ tmp_total_cards - 1 ] = tmp_card;
                    // riordinamento array
                    Arrays.sort( tmp_cards, joker_pos + 1, tmp_total_cards );
                }
                return true;
            }
        }
        return false;
    }

    private int get_straight_score( int tmp_total_cards, int[] tmp_cards ) {
        int score = 0, value = 0, i;
        for( i = 0; i < tmp_total_cards; i++ ) {
            if( tmp_cards[ i ] >= DeckClass.BLACK_JOKER ) {
                if( i == 0 ) {
                    value = ( tmp_cards[ i + 1 ] % 13 ) - 1;
                    if ( value >= 9 ) {
                        score += 10;
                    } else {
                        score += ( value + 1 );
                    }
                } else {
                    value = ( tmp_cards[ i - 1 ] % 13 ) + 1;
                    if ( value >= 13 ) {
                        score += 11;
                    } else if ( value >= 9 ) {
                        score += 10;
                    } else {
                        score += ( value + 1 );
                    }
                }
            } else {
                value = ( tmp_cards[ i ] % 13 );
                if( value == 0 ) {
                    if( i > 0 ) {
                        score += 11;
                    } else {
                        score += 1;
                    }
                } else {
                    if ( value >= 9 ) {
                        score += 10;
                    } else {
                        score += ( value + 1 );
                    }
                }
            }
        }
        return score;
    }

    private int get_straight_joker_value( int joker_count, int tmp_total_cards, int[] tmp_cards ) {
        int joker_value     = -1;
        int previous_card   = 0;
        if( joker_count > 0 ) {
            if( tmp_cards[ 0 ] >= DeckClass.BLACK_JOKER ) {
                joker_value = ( tmp_cards[ 1 ] % 13 ) - 1;
                if( joker_value < 0 ) joker_value = -1;
            } else {
                for( int i = 0; i < tmp_total_cards; i++ ) {
                    if( tmp_cards[ i ] < DeckClass.BLACK_JOKER ) {
                        previous_card = tmp_cards[ i ];
                    } else {
                        break;
                    }
                }
                joker_value = ( previous_card % 13 ) + 1;
                if( joker_value > 12 ) joker_value = 0;
            }
        }
        return joker_value;
    }

    EvaluationResult evaluate_cards( int tmp_total_cards, int[] tmp_cards ) {
        boolean     is_valid    = false;
        int         type        = GroupClass.GROUP_TYPE_UNSET;
        int         score       = 0;
        int         joker_value = -1;

        int[]       card_count      = new int[ 13 ];
        int[]       suit_count      = new int[ 4 ];
        int         joker_count     = 0;
        boolean     ace_flag        = false;
        int         highest_card    = 1;
        int         lowest_card     = 13;
        int         suit            = 0;
        int         card            = 0;
        int         i;

        // il n. minimo di carte per una combinazione valida e' 3
        if( tmp_total_cards >= 3 ) {

            do {
                // azzeramento conteggio valori e semi
                Arrays.fill( card_count, 0 );
                Arrays.fill( suit_count, 0 );
                // conteggio valori, semi, jolly, carta piu' alta, carta piu' bassa e presenza dell'asso
                for( i = 0; i < tmp_total_cards; i++)
                {
                    if( tmp_cards[ i ] >= DeckClass.BLACK_JOKER ) {
                        joker_count += 1;
                    } else {
                        suit = tmp_cards[ i ] / 13;
                        card = tmp_cards[ i ] % 13;

                        card_count[ card ]	+= 1;
                        suit_count[ suit ]  += 1;

                        // se la carta corrente e' un asso setto il flag relativo
                        if( card == DeckClass.ACE_CARD ) {
                            ace_flag = true;
                        } else {
                            // memorizza la carta piu' alta e la piu' bassa
                            if( card < lowest_card  ) lowest_card     = card;
                            if( card > highest_card ) highest_card    = card;
                        }
                    }
                }
                // nel gruppo puo' essere incluso al massimo 1 solo jolly
                if( joker_count > 1 ) {
                    break;
                }

                if( is_valid_combination( card_count, suit_count, joker_count, tmp_total_cards ) ) {
                    is_valid    = true;
                    type        = GroupClass.GROUP_TYPE_COMBINATION;
                    score       = get_combination_score( tmp_total_cards, tmp_cards );
                    joker_value = get_combination_joker_value( joker_count, tmp_total_cards, tmp_cards );
                    break;
                }

                if( is_valid_straight( card_count, suit_count, joker_count, tmp_total_cards, tmp_cards, ace_flag, highest_card, lowest_card ) ) {
                    is_valid    = true;
                    type        = GroupClass.GROUP_TYPE_STRAIGHT;
                    score       = get_straight_score( tmp_total_cards, tmp_cards );
                    joker_value = get_straight_joker_value( joker_count, tmp_total_cards, tmp_cards );
                    break;
                }

            } while( false );
        }

        return new EvaluationResult( is_valid, type, tmp_total_cards, score, joker_value, tmp_cards );
    }

    public int calculate_losing_score() {
        int final_score = 0;
        int card_value  = 0;
        for( int i = 0; i < cards.size(); i++ ) {
            if( cards.get( i ).value >= DeckClass.BLACK_JOKER ) {
                final_score += 25;  // il joker vale 25 punti
            } else {
                card_value = cards.get( i ).value % 13;
                if ( card_value == 0 ) {
                    final_score += 11; // l'Asso vale 11 punti
                } else if ( card_value >= 10 ) {
                    final_score += 10; // le figure valgono 10
                } else {
                    final_score += ( card_value + 1 );  // dal 2 al 9 valgono il valore nominale
                }
            }
        }
        return final_score;
    }
}
