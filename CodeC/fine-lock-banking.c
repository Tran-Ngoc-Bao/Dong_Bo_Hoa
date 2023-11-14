#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <pthread.h>

#define INIT_BALANCE 50
#define NUM_TRANS 200

int balance = INIT_BALANCE;
int credits = 0;
int debits = 0;

pthread_mutex_t b_lock,c_lock,d_lock;

void *transactions(void *args) {
    int i, v;
    for (i = 0; i < NUM_TRANS; i++) {
        // choose a random value
        srand(time(NULL));
        v = rand() % NUM_TRANS;
        // randomnly choose to credit or debit
        if (rand() % 2) {
            // credit
            pthread_mutex_lock(&b_lock);
            balance = balance + v;
            pthread_mutex_unlock(&b_lock);

            pthread_mutex_lock(&c_lock);
            credits = credits + v;
            pthread_mutex_unlock(&c_lock);
        } else {
            // debit
            pthread_mutex_lock(&b_lock);
            balance = balance - v;
            pthread_mutex_unlock(&b_lock);

            pthread_mutex_lock(&d_lock);
            debits = debits + v;
            pthread_mutex_unlock(&d_lock);
        }
    }
    return 0;
}

int main(int argc, char *argv[]) {
    int n_threads, i;
    pthread_t *threads;
    // error check
    if (argc < 2) {
        fprintf(stderr, "ERROR: Require number of threads\n");
        exit(1);
    }
    // convert string to int
    n_threads = atol(argv[1]);
    // error check
    if (n_threads <= 0) {
        fprintf(stderr, "ERROR: Invalivd value for number of threads\n");
        exit(1);
    }
    // allocate array of thread identifiers
    threads = calloc(n_threads, sizeof(pthread_t));

    struct timespec now;
    clock_gettime(CLOCK_REALTIME, &now);
    long start = (now.tv_sec * 1000) + (now.tv_nsec / 1000000);

    pthread_mutex_init(&b_lock, NULL);
    pthread_mutex_init(&c_lock, NULL);
    pthread_mutex_init(&d_lock, NULL);

    // start all threads
    for (i = 0; i < n_threads; i++) {
        pthread_create(&threads[i], NULL, transactions, NULL);
    }
    // wait for all threads finish its jobs
    for (i = 0; i < n_threads; i++) {
        pthread_join(threads[i], NULL);
    }

    clock_gettime(CLOCK_REALTIME, &now);
    long end = (now.tv_sec * 1000) + (now.tv_nsec / 1000000);

    printf("\tCredits:\t%d\n", credits);
    printf("\tDebits:\t\t%d\n\n", debits);
    printf("\t%d+%d-%d=\t%d\n", INIT_BALANCE, credits, debits, INIT_BALANCE + credits - debits);
    printf("\tBalance:\t%d\n\n", balance);

    pthread_mutex_destroy(&b_lock);
    pthread_mutex_destroy(&c_lock);
    pthread_mutex_destroy(&d_lock);

    printf("\tTime:\t\t%ldms\n", end - start);
    
    // free array
    free(threads);
    return 0;
}