import java.io.Serializable;
import java.util.ArrayList;

public class ViewWine implements Serializable {
	Wine wine;
	ArrayList<Listing> listings;

	ViewWine(Wine wine, ArrayList<Listing> listings) {
		this.wine = wine;
		this.listings = listings;
	}
}
