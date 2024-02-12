package com.plmtmobile.scala40se;

import android.util.Log;

import com.plmtmobile.scala40se.GroupClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by daniele on 23/10/13.
 */
public class Strategy {

    public class SimpleCard {
        public int         value;
        SimpleCard ( int value ) {
            this.value = value;
        }
    }

    private static final int    MAX_CARD_VALUES =   13;
    private static final int    MAX_CARD_SUITS  =   4;

    private static final int    ERROR_INVALID_OPENING_COMBINATION   = 1;


    ArrayList<EvaluationResult> candidates = new ArrayList<EvaluationResult>();
    ArrayList<EvaluationResult> ready_combs = new ArrayList<EvaluationResult>();

    Strategy() {
    }

    private void fetch_player_cards( PlayerClass p, ArrayList<SimpleCard> cards ) {
        cards.clear();
        for( int i = 0; i < p.cards.size(); i++ ) {
            cards.add( new SimpleCard( p.cards.get( i ).value ) );
        }
    }

    private void fill_candidates_list( PlayerClass p, ArrayList<SimpleCard> cardscopy, int cards_count[][], int joker_count ) {
        int total = 0;
        int joker = 0;
        int tmp_total_cards;
        int tmp_cards[] = new int [ PlayerClass.MAX_PLAYER_CARDS ];
        int i, j;

        if( joker_count > 0 ) joker = 1;

        candidates.clear();

        // ricerca di tutti i possibili tris o poker
        for( j = 0; j < MAX_CARD_VALUES; j++ ) {
            total = 0;
            for( i = 0; i < MAX_CARD_SUITS; i++ ) {
                if( cards_count[ i ][ j ] > 0 ) total += 1;
            }
            if( ( total + joker ) >= 3 ) {
                tmp_total_cards = 0;
                for( i = 0; i < MAX_CARD_SUITS; i++ ) {
                    if( cards_count[ i ][ j ] > 0 ) {
                        tmp_cards[ tmp_total_cards ] = i * MAX_CARD_VALUES + j;
                        tmp_total_cards += 1;
                    }
                }
                if( joker > 0 ) {
                    tmp_cards[ tmp_total_cards ] = DeckClass.BLACK_JOKER;
                    tmp_total_cards += 1;
                }
                // aggiunge le carte alla lista dei candidati
                candidates.add( p.evaluate_cards( tmp_total_cards, tmp_cards ) );
            }
        }
        // ricerca di tutte le possibili scale
        int card_count_copy[][] = new int [ MAX_CARD_SUITS ][ MAX_CARD_VALUES + 1 ];
        int consecutives    = 0;
        int cons_status     = 0;

        for( i = 0; i < MAX_CARD_SUITS; i++ )
            for( j = 0; j < MAX_CARD_VALUES; j++ )
                card_count_copy[ i ][ j ] = cards_count[ i ][ j ];
        j = MAX_CARD_VALUES;
        for( i = 0; i < MAX_CARD_SUITS; i++ )
            card_count_copy[ i ][ j ] = cards_count[ i ][ 0 ];

        for( i = 0; i < MAX_CARD_SUITS; i++ ) {
            // dall'Asso al K
            consecutives    = 0;
            cons_status     = 0;
            for( j = 0; j < ( MAX_CARD_VALUES + 1 ); j++ ) {
                if( card_count_copy[ i ][ j ] > 0 ) {
                    if( j == MAX_CARD_VALUES ) {
                        tmp_cards[ consecutives ] = i * MAX_CARD_VALUES + 0;
                    } else {
                        tmp_cards[ consecutives ] = i * MAX_CARD_VALUES + j;
                    }
                    consecutives    += 1;
                    cons_status     = 1;
                } else {
                    if( ( cons_status == 1 ) && ( consecutives <= 13 ) ) {
                        if( joker > 0 ) {
                            tmp_cards[ consecutives ] = DeckClass.BLACK_JOKER;
                        }
                        if( ( consecutives + joker ) >= 3 ) {
                            tmp_total_cards = consecutives + joker;
                            // aggiunge le carte alla lista dei candidati
                            candidates.add( p.evaluate_cards( tmp_total_cards, tmp_cards ) );
                        }
                    }
                    consecutives    = 0;
                    cons_status     = 0;
                }
            }
            // questo controllo chiude le scale fino al K
            if( ( cons_status == 1 ) && ( consecutives <= 13 ) ) {
                if( joker > 0 ) {
                    tmp_cards[ consecutives ] = DeckClass.BLACK_JOKER;
                }
                if( ( consecutives + joker ) >= 3 ) {
                    tmp_total_cards = consecutives + joker;
                    // aggiunge le carte alla lista dei candidati
                    candidates.add( p.evaluate_cards( tmp_total_cards, tmp_cards ) );
                }
            }
        }
    }

    private int count_cards( ArrayList<SimpleCard> cards, int cards_count[][] ) {
        int joker_count = 0;

        for( int i = 0; i < MAX_CARD_SUITS; i++ )
            for( int j = 0; j < MAX_CARD_VALUES; j++ )
                cards_count[ i ][ j ] = 0;

        for( int i = 0; i < cards.size(); i++ ) {
            if( cards.get( i ).value >= DeckClass.BLACK_JOKER ) {
                joker_count += 1;
            } else {
                cards_count[ cards.get( i ).value / 13 ][ cards.get( i ).value % 13 ] += 1;
            }
        }
        return joker_count;
    }

    private void sort_candidates_list() {
        Collections.sort( candidates, new CandidatesComparator() );
    }

    private void fill_ready_combs_list( PlayerClass p, ArrayList<SimpleCard> cards ) {

        ArrayList<SimpleCard>   cardscopy   = null;
        int                     cards_count[][] = new int [ MAX_CARD_SUITS ][ MAX_CARD_VALUES ];
        int                     joker_count = 0;

        cardscopy = new ArrayList<SimpleCard>();
        cardscopy.clear();
        for( int i = 0; i < cards.size(); i++ ) {
            cardscopy.add( new SimpleCard( cards.get( i ).value ) );
        }

        ready_combs.clear();
        do {
            // calcola matrice occorrenze per ogni tipo di carta
            joker_count = count_cards( cardscopy, cards_count );
            // riempie la lista con tutte le possibili combinazioni ottenibili
            fill_candidates_list(p, cardscopy, cards_count, joker_count);
            // ordina la lista dei candidati in base al punteggio
            if( candidates.size() > 0 ) {
                sort_candidates_list();
            } else {
                break;
            }
            // se il n. di carte rimaste e' inferiore a comporre una combinazione valida (3) fine ricerca
            if( cardscopy.size() < 3 ) {
                break;
            }
            // aggiunge la combinazione migliore alla lista delle combinazioni pronte
            ready_combs.add( candidates.get( 0 ) );
            // bisogna rimuovere da cardscopy le carte della combinazione appena aggiunta
            // per ogni carta della combinazione trovata
            for( int i = 0; i < candidates.get( 0 ).total_cards; i++ ) {
                // scorro a ritroso tutte le carte di cardscopy
                for( int k = ( cardscopy.size() - 1 ); k >= 0; k-- ) {
                    // se trovo la carta corrispondente a quella della combinazione...
                    if( ( ( candidates.get( 0 ).cards[ i ] >= DeckClass.BLACK_JOKER ) && ( cardscopy.get( k ).value >= DeckClass.BLACK_JOKER ) ) ||
                        ( cardscopy.get( k ).value == candidates.get( 0 ).cards[ i ] ) ) {
                        // ...la rimuovo...
                        cardscopy.remove( k );
                        // ...e passo alla carta successiva
                        break;
                    }
                }
            }

        } while( true );
    }

    private int get_discard_position( PlayerClass p ) {
        ArrayList<SimpleCard>   cardscopy       = null;
        int                     cards_count[][] = new int [ MAX_CARD_SUITS ][ MAX_CARD_VALUES ];
        int                     cards_weights[][] = new int [ MAX_CARD_SUITS ][ MAX_CARD_VALUES ];

        cardscopy = new ArrayList<SimpleCard>();
        cardscopy.clear();
        for( int i = 0; i < p.cards.size(); i++ ) {
            cardscopy.add( new SimpleCard( p.cards.get( i ).value ) );
        }
        // conta le carte
        count_cards( cardscopy, cards_count );

        // la prima cosa da eliminare sono i doppioni
        for( int i = 0; i < MAX_CARD_SUITS; i++ )
            for( int j = 0; j < MAX_CARD_VALUES; j++ )
                if( cards_count[ i ][ j ] > 1 ) {
                    for( int k = 0; k < p.cards.size(); k++ ) {
                        if( p.cards.get( k ).value == ( i * MAX_CARD_VALUES + j ) )
                            return k;
                    }
                }
        for( int i = 0; i < MAX_CARD_SUITS; i++ )
            for( int j = 0; j < MAX_CARD_VALUES; j++ )
                cards_weights[ i ][ j ] = 100;
        // assegnazione pesi per ogni carta
        // ricerca coppie, tris e poker per ogni valore
        for( int j = 0; j < MAX_CARD_VALUES; j++ ) {
            // conta il numero totale di occorrenze
            int total = 0;
            for( int i = 0; i < MAX_CARD_SUITS; i++ )
                if( cards_count[ i ][ j ] > 0 ) {
                    total += 1;
                }
            for( int i = 0; i < MAX_CARD_SUITS; i++ )
                if( cards_count[ i ][ j ] > 0 ) {
                    cards_weights[ i ][ j ] = total;
                }
        }
        // calcolo pesi per le scale
        for( int i = 0; i < MAX_CARD_SUITS; i++ )
            for( int j = 0; j < MAX_CARD_VALUES; j++ ) {
                if( cards_count[ i ][ j ] > 1 ) {
                    int total = 0;
                    if( ( ( j - 2 ) >= 0 ) && ( cards_count[ i ][ j - 2 ] > 0 ) )
                        total += 1;
                    if( ( ( j - 1 ) >= 0 ) && ( cards_count[ i ][ j - 1 ] > 0 ) )
                        total += 2;
                    if( ( ( j + 1 ) < MAX_CARD_VALUES ) && ( cards_count[ i ][ j + 1 ] > 0 ) )
                        total += 2;
                    if( ( ( j + 2 ) < MAX_CARD_VALUES ) && ( cards_count[ i ][ j + 2 ] > 0 ) )
                        total += 1;
                    cards_weights[ i ][ j ] += total;
                }
            }

        int min_weight      = 100;
        int min_weight_i    = 0;
        int min_weight_j    = 0;
        for( int i = 0; i < MAX_CARD_SUITS; i++ )
            for( int j = 0; j < MAX_CARD_VALUES; j++ )
                if( cards_weights[ i ][ j ] < min_weight ) {
                    min_weight      = cards_weights[ i ][ j ];
                    min_weight_i    = i;
                    min_weight_j    = j;
                }
        // scarta la carta con meno peso
        for( int k = 0; k < p.cards.size(); k++ ) {
            if( p.cards.get( k ).value == ( min_weight_i * MAX_CARD_VALUES + min_weight_j ) )
                return k;
        }
        return 0;
    }

    private String LogCardToString( int card_value ) {
        String s = new String();

        if( card_value >= DeckClass.BLACK_JOKER ) {
            return s.concat( new String( "W " ) );
        } else {
            switch( card_value % 13 ) {
                case 0: s.concat( new String( "A" ) ); break;
                case 10: s.concat( new String( "J")  ); break;
                case 11: s.concat( new String( "Q")  ); break;
                case 12: s.concat( new String( "K")  ); break;
                default: s.concat( String.format( "%d", ( card_value % 13 ) + 1 ) ); break;
            }
            switch( card_value / 13 ) {
                case 0: s.concat( new String( "c")  ); break;
                case 1: s.concat( new String( "q")  ); break;
                case 2: s.concat( new String( "p")  ); break;
                case 3: s.concat( new String( "f")  ); break;
            }
        }
        return s;
    }

    public StrategyResult play( PlayerClass p, DeckClass deck, ArrayList<GroupClass> groups ) {

        ArrayList<SimpleCard>   cards               = new ArrayList<SimpleCard>();
        int                     total_score         = 0;
        int                     total_cards         = 0;
        boolean                 str_device_opened   = false;
        int                     str_opening_score   = 0;
        boolean                 str_device_newgroup = false;
        boolean                 str_device_attach   = false;
        boolean                 str_discard_failed  = false;
        int                     error               = 0;
        int                     cull_card           = 0;

        if( p.has_opened == false ) {

            // l'avversario non ha ancora aperto
            Log.v( Errors.logfilter, "Strategia: apertura" );

            // verifica se posso aprire con la carta nel pozzo
            boolean pick_from_cull = false;

            // controlla se e' utile la carta nel pozzo
            // verifico se la carta del pozzo mi e' utile
            cull_card = deck.cull_cards[ deck.cull_size - 1 ];

            // verifico se la carta nel pozzo puo' creare combinazioni complete con le carte in mano all'avversario

            // riempie il vettore cards con tutte le carte in mano al giocatore
            fetch_player_cards( p, cards );

            // aggiungo (VIRTUALMENTE, non l'ho ancora pescata!) la carta del pozzo alle carte del giocatore
            cards.add( new SimpleCard( cull_card ) );
            // e riempio la lista con le "migliori" combinazioni componibili
            fill_ready_combs_list(p, cards);
            // se ci sono combinazioni componibili calcolo:
            // - il punteggio totale ottenibile
            // - il n. di carte impiegate dalla prima combinazione ottenibile
            total_score = 0;
            total_cards = 0;
            boolean cull_card_in_groups = false;
            for( int i = 0; i < ready_combs.size(); i++ ) {
                total_score += ready_combs.get( i ).score;
                total_cards += ready_combs.get( i ).total_cards;
                if( cull_card_in_groups == false ) {
                    for( int k = 0; k < ready_combs.get( i ).total_cards; k++ ) {
                        if( ready_combs.get( i ).cards[ k ] == cull_card ) {
                            cull_card_in_groups = true;
                            break;
                        }
                    }
                }
            }

            // se
            // - ho un punteggio maggiore o uguale a 40,
            // - la carta pescata dal pozzo e' tra le carte da calare
            // - mi resta almeno 1 carte per lo scarto
            if( ( total_score >= PlayerClass.OPENING_SCORE ) && ( cull_card_in_groups ) && ( ( 14 - total_cards ) >= 1 ) ) {
                // OK! usa la carta del pozzo
                cull_card = deck.pick_from_cull();
                p.cards.add( new PlayerClass.PlayerCard( cull_card, false ) );
                p.has_picked = true;
                p.card_picked = cull_card;
                p.picked_from = PlayerClass.PICKED_FROM_CULL;

                // l'avversario apre piazzando le combinazioni sul tavolo
                for( int i = 0; i < ready_combs.size(); i++ ) {
                    // annulla qualunque selezione precedente
                    p.reset_cards_selection();
                    p.selectedCards.clear();
                    // seleziona solo le carte appartenenti alla combinazione
                    for( int k = 0; k < ready_combs.get( i ).total_cards; k++ ) {
                        for( int pos = 0; pos < p.cards.size(); pos++ ) {
                            if( ( ( ready_combs.get( i ).cards[ k ] == DeckClass.BLACK_JOKER ) && ( p.cards.get( pos ).value >= DeckClass.BLACK_JOKER ) ) ||
                                    ( p.cards.get( pos ).value == ready_combs.get( i ).cards[ k ] ) ) {
                                p.select_card( pos );
                                break;
                            }
                        }
                    }
                    // la table_action aggiunge le combinazioni al tavolo
                    TableActionResult r = p.table_action( Graphics.EMPTY_TABLE_CARD, groups );
                    if( r.error > 0 ) {
                        error = r.error;
                    }
                }
                str_device_opened = true;
                str_opening_score = total_score;

                pick_from_cull = true;
            }

            if( pick_from_cull == false ) {
                // pesca 1 carta dal tallone
                p.pick_from_pool( deck );
                // riempie il vettore cards con tutte le carte in mano all'avversario
                fetch_player_cards( p, cards );
                // riempie la lista con le "migliori" combinazioni componibili
                fill_ready_combs_list( p, cards );
                // se ci sono combinazioni componibili ne calcola il punteggio totale
                total_score = 0;
                total_cards = 0;
                for( int i = 0; i < ready_combs.size(); i++ ) {
                    total_score += ready_combs.get( i ).score;
                    total_cards += ready_combs.get( i ).total_cards;
                    //Log.v( Errors.logfilter, String.format( "ready_combs %d = punti %d", i, ready_combs.get( i ).score ) );
                }
                // se il punteggio e' uguale o maggiore di 40 e mi rimane almeno 1 carta per lo scarto
                if( ( total_score >= PlayerClass.OPENING_SCORE ) && ( ( 14 - total_cards ) >= 1 ) ) {
                    // l'avversario apre piazzando le combinazioni sul tavolo
                    for( int i = 0; i < ready_combs.size(); i++ ) {
                        // annulla qualunque selezione precedente
                        p.reset_cards_selection();
                        p.selectedCards.clear();
                        // seleziona solo le carte appartenenti alla combinazione
                        for( int k = 0; k < ready_combs.get( i ).total_cards; k++ ) {
                            for( int pos = 0; pos < p.cards.size(); pos++ ) {
                                if( ( ( ready_combs.get( i ).cards[ k ] == DeckClass.BLACK_JOKER ) && ( p.cards.get( pos ).value >= DeckClass.BLACK_JOKER ) ) ||
                                        ( p.cards.get( pos ).value == ready_combs.get( i ).cards[ k ] ) ) {
                                    p.select_card( pos );
                                    break;
                                }
                            }
                        }
                        // la table_action aggiunge le combinazioni al tavolo
                        TableActionResult r = p.table_action( Graphics.EMPTY_TABLE_CARD, groups );
                        if( r.error > 0 ) {
                            error = r.error;
                        }
                    }
                    str_device_opened = true;
                    str_opening_score = total_score;
                }
            }

            // scarta 1 carta tra le rimanenti in mano all'avversario
            p.select_card( get_discard_position( p ) );
            CullActionResult cullActionResult = p.cull_action( deck, groups );
            if( cullActionResult.discarded_card == false ) {
                str_discard_failed = true;
            }

        } else {

            // l'avversario ha gia' aperto

            // valuto se pescare dal pozzo o dal tallone:
            // se la carta del pozzo e' un joker oppure
            // se la carta del pozzo mi permette di recuperare un joker dal tavolo oppure
            // se la carta del pozzo mi permette di creare una nuova combinazione da aggiungere al tavolo pur rimanendo con almeno 1 carta per lo scarto
            // pesco la carta del pozzo altrimenti pesco dal tallone

            // cull_card e' la carta del pozzo
            cull_card = deck.cull_cards[ deck.cull_size - 1 ];

            // verifico se la carta nel pozzo mi e' utile per recuperare un joker dal tavolo
            // in teoria questa situazione non dovrebbe mai accadere in quanto un giocatore (a meno di un errore) non scarterebbe mai
            // una carta sostituibile con un joker all'interno di uno dei gruppi gia' presenti sul tavolo, ma non si sa mai...
            int[]   tmp_cards               = new int[ PlayerClass.MAX_PLAYER_CARDS ];
            boolean joker_retrieve_useful   = false;
            // se la carta nel pozzo NON e' un joker
            if( cull_card < DeckClass.BLACK_JOKER ) {
                // verifico ogni gruppo presente sul tavolo
                for( int i = 0; i < groups.size(); i++ ) {
                    // se
                    // - il gruppo sul tavolo contiene un joker
                    // - la carta del pozzo ha lo stesso valore nominale della carta sostituita dal joker
                    if( ( groups.get( i ).joker_value >= 0 ) && ( ( cull_card % 13 ) == groups.get( i ).joker_value ) )
                    {
                        // sostituisco la carta del pozzo al joker...
                        for( int j = 0; j < groups.get( i ).total_cards; j++ ) {
                            if( groups.get( i ).cards[ j ] >= DeckClass.BLACK_JOKER )
                                tmp_cards[ j ] = cull_card;
                            else
                                tmp_cards[ j ] = groups.get( i ).cards[ j ];
                        }
                        // ...e valuto se la combinazione e' valida
                        EvaluationResult eval = p.evaluate_cards( groups.get( i ).total_cards, tmp_cards );
                        // se la combinazione e' valida la carta del pozzo e' utile per recuperare un joker
                        if( eval.is_valid ) {
                            joker_retrieve_useful = true;
                            break;
                        }
                    }
                }
            }


            // verifico se la carta nel pozzo e' attaccabile a uno dei gruppi gia' presenti sul tavolo,
            // sempre ipotizzando che sia stata scartata per errore dal giocatore
            boolean attachable_cull_card = false;
            // verifico ogni gruppo
            for( int k = 0; k < groups.size(); k++ ) {
                // metto in tmp_cards le carte del gruppo
                for( int j = 0; j < groups.get( k ).total_cards; j++ ) {
                    tmp_cards[ j ] = groups.get( k ).cards[ j ];
                }
                // aggiungo a tmp_cards la carta del pozzo
                tmp_cards[ groups.get( k ).total_cards ] = cull_card;
                // aggiorno il totale (n. carte gruppo + 1)
                total_cards = groups.get( k ).total_cards + 1;
                // e valuto la combinazione
                EvaluationResult result3 = p.evaluate_cards( total_cards, tmp_cards );
                // se la combinazione e' valida la carta nel pozzo e' attaccabile
                if( result3.is_valid ) {
                    attachable_cull_card = true;
                }
            }

            // verifico se la carta nel pozzo puo' creare combinazioni complete con le carte in mano all'avversario
            // riempie il vettore cards con tutte le carte in mano all'avversario
            fetch_player_cards( p, cards );
            // aggiungo (VIRTUALMENTE, non l'ho ancora pescata!) la carta del pozzo alle carte del giocatore
            cards.add( new SimpleCard( cull_card ) );
            // e riempio la lista con le "migliori" combinazioni componibili
            fill_ready_combs_list(p, cards);
            // se ci sono combinazioni componibili calcolo:
            // - il punteggio totale ottenibile
            // - il n. di carte impiegate da tutte le combinazioni ottenibili
            total_score = 0;
            total_cards = 0;
            for( int i = 0; i < ready_combs.size(); i++ ) {
                total_score += ready_combs.get( i ).score;
            }
            if( ready_combs.size() > 0 ) {
                total_cards = ready_combs.get( 0 ).total_cards;
            }


            if( ( cull_card >= DeckClass.BLACK_JOKER )  ||      // se la carta nel pozzo e' un joker
                ( attachable_cull_card == true )        ||      // se la carta e' attaccabile a uno dei gruppi gia' presenti sul tavolo
                (  joker_retrieve_useful == true )       ||      // se mi e' utile per recuperare un joker dal tavolo
                ( ( total_score > 0 ) && ( ( cards.size() - total_cards ) >= 1 ) ) // mi permette di creare combinazioni da aggiungere al tavolo
                ) {
                // pesco dal pozzo
                p.cull_action( deck, groups );
            } else {
                // altrimenti pesco dal tallone
                // la carta viene gia' aggiunta a quelle in mano al giocatore
                p.pick_from_pool( deck );
            }

            // AGGIUNGO (SE POSSIBILE) NUOVE COMBINAZIONI CHE POSSO CREARE CON LA CARTA PESCATA DAL POZZO
            // riempie il vettore cards con tutte le carte in mano all'avversario
            fetch_player_cards( p, cards );
            // e riempio la lista con le "migliori" combinazioni componibili
            fill_ready_combs_list(p, cards);
            // se ci sono combinazioni componibili le aggiungo al tavolo
            for( int i = 0; i < ready_combs.size(); i++ ) {
                // se tolte le carte della combinazione mi rimane almeno 1 carta da scartare..OK!
                if( ( p.cards.size() - ready_combs.get( i ).total_cards ) >= 1 ) {
                    // annullo qualunque selezione precedente
                    p.reset_cards_selection();
                    p.selectedCards.clear();
                    // seleziono solo le carte appartenenti alla combinazione da piazzare
                    for( int k = 0; k < ready_combs.get( i ).total_cards; k++ ) {
                        for( int pos = 0; pos < p.cards.size(); pos++ ) {
                            if( ( ( ready_combs.get( i ).cards[ k ] == DeckClass.BLACK_JOKER ) && ( p.cards.get( pos ).value >= DeckClass.BLACK_JOKER ) ) ||
                                    ( p.cards.get( pos ).value == ready_combs.get( i ).cards[ k ] ) ) {
                                p.select_card( pos );
                                break;
                            }
                        }
                    }
                    // e piazzo la combinazione sul tavolo
                    TableActionResult r = p.table_action( Graphics.EMPTY_TABLE_CARD, groups );

                    // flag "aggiunta nuovo gruppo"
                    str_device_newgroup = true;
                }
            }

            // flag recupero joker
            boolean joker_acquired  = false;


            // CERCO DI ATTACCARE CARTE E/O RECUPARE JOKER DAI GRUPPI GIA' PRESENTI SUL TAVOLO

            // se ho almeno 2 carte (1 mi deve rimanere per lo scarto)
            if( p.cards.size() > 1 ) {
                // per ogni carta in mano all'avversario
                for( int i = ( p.cards.size() - 1 ); i >= 0; i-- ) {

                    // verifico se e' attaccabile a uno dei gruppi gia' presenti sul tavolo
                    for( int k = 0; k < groups.size(); k++ ) {

                        // VERIFICO SE E' ATTACCABILE ALLE CARTE GIA' ESISTENTI

                        // inserisco nel vettore temporaneo tmp_cards le carte del gruppo
                        boolean joker_inside = false;
                        for( int j = 0; j < groups.get( k ).total_cards; j++ ) {
                            tmp_cards[ j ] = groups.get( k ).cards[ j ];
                            // flag che indica se il gruppo contiene un joker
                            if( tmp_cards[ j ] >= DeckClass.BLACK_JOKER ) {
                                joker_inside = true;
                            }
                        }
                        // aggiungo in coda alle carte del gruppo la carta in esame
                        tmp_cards[ groups.get( k ).total_cards ] = p.cards.get( i ).value;
                        // aggiorno il n. totale di carte (carte del gruppo + 1)
                        total_cards = groups.get( k ).total_cards + 1;
                        // valuto la combinazione
                        EvaluationResult result1 = p.evaluate_cards( total_cards, tmp_cards );
                        // se la combinazione e' valida provo a eseguire l'attacco
                        if( result1.is_valid ) {
                            // annullo qualunque selezione precedente
                            p.reset_cards_selection();
                            p.selectedCards.clear();
                            // seleziono la carta..
                            p.select_card( i );
                            // ..e tento l'attacco
                            TableActionResult r = p.table_action( k, groups );
                            if( r.error == 0 ) {
                                str_device_attach = true;
                                // fine controllo gruppi, passo alla prossima carta in mano da esaminare
                                break;
                            }
                        }
                        // provo a sostituire il joker con la carta in esame
                        if( joker_inside ) {
                            // inserisco nel vettore temporaneo tmp_cards le carte del gruppo
                            for( int j = 0; j < groups.get( k ).total_cards; j++ ) {
                                tmp_cards[ j ] = groups.get( k ).cards[ j ];
                                // quando trovo il joker lo sostituisco con la carta in esame
                                if( tmp_cards[ j ] >= DeckClass.BLACK_JOKER ) {
                                    tmp_cards[ j ] = p.cards.get( i ).value;
                                }
                            }
                            // il n. totale di carte rimane identico al n. di carte del gruppo
                            total_cards = groups.get( k ).total_cards;
                            // valuto la combinazione
                            EvaluationResult result2 = p.evaluate_cards( total_cards, tmp_cards );
                            // se la combinazione e' valida provo a eseguire la sostituzione del jolly
                            if( result2.is_valid ) {
                                // annullo qualunque selezione precedente
                                p.reset_cards_selection();
                                p.selectedCards.clear();
                                // seleziono la carta..
                                p.select_card( i );
                                // ..salvo i valori per determinare l'acquisizione di un joker..
                                int old_group_total_cards = groups.get( k ).total_cards;
                                int old_group_joker_value = groups.get( k ).joker_value;
                                // ..e tento l'attacco
                                TableActionResult r = p.table_action( k, groups );
                                if( r.error == 0 ) {
                                    // controllo se ho acquisito un joker
                                    if( old_group_total_cards == groups.get( k ).total_cards ) {
                                        if( ( old_group_joker_value >= 0 ) && ( groups.get( k ).joker_value < 0 ) ) {
                                            joker_acquired = true;
                                        }
                                    }
                                    str_device_attach = true;
                                    // fine controllo gruppi, passo alla prossima carta in mano da esaminare
                                    break;
                                }
                            }
                        }

                    }
                    // muovendo una carta per volta in teoria qui dovrebbe arrivarci con p.cards.size == 1
                    if( p.cards.size() <= 1 ) {
                        // fine controllo carte in mano
                        break;
                    }
                }
            }

            // se tra le carte in mano al giocatore sono ancora presenti dei joker
            if( joker_acquired == false ) {
                for( int i = 0; i < p.cards.size(); i++ ) {
                    if( p.cards.get( i ).value >= DeckClass.BLACK_JOKER ) {
                        joker_acquired = true;
                        break;
                    }
                }
            }

            // se e' stato recuperato un joker dai gruppi sul tavolo...
            if( joker_acquired ) {

                // VERIFICO SE POSSO CREARE NUOVE COMBINAZIONI CON LE CARTE IN MANO

                // riempie il vettore cards con tutte le carte in mano all'avversario
                fetch_player_cards( p, cards );
                // e riempio la lista con le "migliori" combinazioni componibili
                fill_ready_combs_list(p, cards);
                // se ci sono combinazioni componibili le aggiungo al tavolo
                for( int i = 0; i < ready_combs.size(); i++ ) {
                    // se tolte le carte della combinazione mi rimane almeno 1 carta da scartare..OK!
                    if( ( p.cards.size() - ready_combs.get( i ).total_cards ) >= 1 ) {
                        // annullo qualunque selezione precedente
                        p.reset_cards_selection();
                        p.selectedCards.clear();
                        // seleziono solo le carte appartenenti alla combinazione da piazzare
                        for( int k = 0; k < ready_combs.get( i ).total_cards; k++ ) {
                            for( int pos = 0; pos < p.cards.size(); pos++ ) {
                                if( ( ( ready_combs.get( i ).cards[ k ] == DeckClass.BLACK_JOKER ) && ( p.cards.get( pos ).value >= DeckClass.BLACK_JOKER ) ) ||
                                        ( p.cards.get( pos ).value == ready_combs.get( i ).cards[ k ] ) ) {
                                    p.select_card( pos );
                                    break;
                                }
                            }
                        }
                        // e piazzo la combinazione sul tavolo
                        TableActionResult r = p.table_action( Graphics.EMPTY_TABLE_CARD, groups );
                        if( r.error == 0 ) {
                            // flag "aggiunta nuovo gruppo"
                            str_device_newgroup = true;
                        }
                    }
                }

                // VERIFICO PER OGNI CARTA RIMASTA IN MANO SE E' ATTACCABILE A UNO DEI GRUPPI GIA' PRESENTI SUL TAVOLO
                if( p.cards.size() > 1 ) {
                    // per ogni carta in mano all'avversario
                    for( int i = ( p.cards.size() - 1 ); i >= 0; i-- ) {
                        // verifico se e' attaccabile a uno dei gruppi gia' presenti sul tavolo
                        for( int k = 0; k < groups.size(); k++ ) {
                            // inserisco nel vettore temporaneo tmp_cards le carte del gruppo
                            for( int j = 0; j < groups.get( k ).total_cards; j++ ) {
                                tmp_cards[ j ] = groups.get( k ).cards[ j ];
                            }
                            // aggiungo in coda alle carte del gruppo la carta in esame
                            tmp_cards[ groups.get( k ).total_cards ] = p.cards.get( i ).value;
                            total_cards = groups.get( k ).total_cards + 1;
                            // valuto la combinazione
                            EvaluationResult result = p.evaluate_cards( total_cards, tmp_cards );
                            // se la combinazione e' valida provo a eseguire l'attacco
                            if( result.is_valid ) {
                                // annullo qualunque selezione precedente
                                p.reset_cards_selection();
                                p.selectedCards.clear();
                                // seleziono la carta..
                                p.select_card( i );
                                // ..e tento l'attacco
                                TableActionResult r = p.table_action( k, groups );
                                if( r.error == 0 ) {
                                    str_device_attach = true;
                                    // fine controllo gruppi, passo alla prossima carta in mano da esaminare
                                    break;
                                }
                            }
                        }
                        // muovendo una carta per volta in teoria qui dovrebbe arrivarci con p.cards.size == 1
                        if( p.cards.size() <= 1 ) {
                            // fine controllo carte in mano per tentativi di attacco
                            break;
                        }
                    }
                }
            }

            // scarta 1 carta tra le rimanenti in mano all'avversario
            p.reset_cards_selection();
            p.selectedCards.clear();
            p.select_card( get_discard_position( p ) );
            CullActionResult cullActionResult = p.cull_action( deck, groups );
            if( cullActionResult.discarded_card == false ) {
                str_discard_failed = true;
            }

        }
        return new StrategyResult( str_device_opened, str_opening_score, str_device_newgroup, str_device_attach, error, str_discard_failed );
    }
}
