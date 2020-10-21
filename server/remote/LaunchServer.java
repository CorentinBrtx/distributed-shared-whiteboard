package remote;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LaunchServer {

	public static void main(String[] args) {

		try {

			// Export the remote math object to the Java RMI runtime so that it
			// can receive incoming remote calls.
			// Because RemoteMath extends UnicastRemoteObject, this
			// is done automatically when the object is initialized.
			//
			// RemoteMath obj = new RemoteMath();
			// IRemoteMath stub = (IRemoteMath) UnicastRemoteObject.exportObject(obj, 0);
			//
			WhiteBoardServer server = new WhiteBoardServer("coco");

			// Publish the remote object's stub in the registry under the name "Compute"
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("WhiteBoardServer", server);

			System.out.println("WhiteBoard server ready");

			// The server will continue running as long as there are remote objects exported
			// into
			// the RMI runtime, to re move remote objects from the
			// RMI runtime so that they can no longer accept RMI calls you can use:
			// UnicastRemoteObject.unexportObject(remoteMath, false);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
