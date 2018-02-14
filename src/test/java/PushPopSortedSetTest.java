import com.lolski.ignite.KeyspaceDoesNotExistInKeyspaceToIndicesException;
import com.lolski.ignite.PushPopSortedSet;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class PushPopSortedSetTest {
    @Test
    public void shouldPutCorrectly() {
        try (Ignite ignite = Ignition.start()) {
            PushPopSortedSet pushPopSortedSet = new PushPopSortedSet("a-random-cache-name").getOrCreate(ignite);

            pushPopSortedSet.put(ignite.transactions(), "keyspace", "1");
            pushPopSortedSet.put(ignite.transactions(), "keyspace", "2");

            assertThat(pushPopSortedSet.getAll("keyspace"), equalTo(new TreeSet<>(Arrays.asList("1", "2"))));
        }
    }

    @Test
    public void shouldNotHaveDuplicate() {
        try (Ignite ignite = Ignition.start()) {
            PushPopSortedSet pushPopSortedSet = new PushPopSortedSet("a-random-cache-name").getOrCreate(ignite);

            pushPopSortedSet.put(ignite.transactions(), "keyspace", "1");
            pushPopSortedSet.put(ignite.transactions(), "keyspace", "1");

            assertThat(pushPopSortedSet.getAll("keyspace"), equalTo(new TreeSet<>(Arrays.asList("1"))));
        }
    }

    @Test
    public void shouldPopNonEmptySetCorrectly() {
        try (Ignite ignite = Ignition.start()) {
            PushPopSortedSet pushPopSortedSet = new PushPopSortedSet("a-random-cache-name").getOrCreate(ignite);

            // initialize with 2 element, and pop one of them
            pushPopSortedSet.put(ignite.transactions(), "keyspace", "1");
            pushPopSortedSet.put(ignite.transactions(), "keyspace", "2");
            String pop = pushPopSortedSet.pop(ignite.transactions(), "keyspace");

            SortedSet<String> setWithoutThePoppedElement = new TreeSet<>(Arrays.asList("1", "2"));
            setWithoutThePoppedElement.remove(pop);

            assertThat(pushPopSortedSet.getAll("keyspace"), equalTo(setWithoutThePoppedElement));
        }
    }

    @Test(expected = KeyspaceDoesNotExistInKeyspaceToIndicesException.class)
    public void shouldThrow_whenPoppingFromANonExistingKeyspace() {
        try (Ignite ignite = Ignition.start()) {
            PushPopSortedSet emptyPushPopSortedSetCollection = new PushPopSortedSet("a-random-cache-name").getOrCreate(ignite);

            String pop = emptyPushPopSortedSetCollection.pop(ignite.transactions(), "keyspace");

            SortedSet<String> setWithoutThePoppedElement = new TreeSet<>(Arrays.asList("1", "2"));
            setWithoutThePoppedElement.remove(pop);

            assertThat(emptyPushPopSortedSetCollection.getAll("keyspace"), equalTo(setWithoutThePoppedElement));
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrow_whenPoppingFromAnAlreadyEmptyCollection() {
        // initialize with 1 element, and pop twice
        try (Ignite ignite = Ignition.start()) {
            PushPopSortedSet pushPopSortedSet = new PushPopSortedSet("a-random-cache-name").getOrCreate(ignite);

            pushPopSortedSet.put(ignite.transactions(), "keyspace", "1");

            pushPopSortedSet.pop(ignite.transactions(), "keyspace");
            pushPopSortedSet.pop(ignite.transactions(), "keyspace");
        }
    }
}