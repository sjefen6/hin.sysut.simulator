package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulator {
	public static final String Simulator_Work_Status_High = "High";
	public static final String Simulator_Work_Status_Medium = "Medium";
	public static final String Simulator_Work_Status_Low = "Low";
	public static final String Simulator_Off = "Off";

	private final int Simulation_High_Limit;
	private final int Simulation_Low_Limit;
	private final int Number_Of_Threads_Per_Processor = 10;

	private boolean active = true;

	public Simulator() {
		Runtime runTime = Runtime.getRuntime();
		this.Simulation_High_Limit = runTime.availableProcessors()
				* Number_Of_Threads_Per_Processor;
		this.Simulation_Low_Limit = Simulation_High_Limit * 3 / 4;

		System.out.println("Starting simulator-thread...");
		new Thread(new ImprovedSimulation()).start();
	}

	/**
	 * TODO:
	 */
	private class ImprovedSimulation implements Runnable {
		public void run() {
			int simulator_id = Settings.getSimulatorID();
			System.out.println("My simulator id is :" + simulator_id);

			while (active) {
				validateSimulatorStatus();
				sleep();
				doSimulations();
			}

			System.out.println("Turning off simulator...");
			try {
				AliveMessenger.getInstance().setStatus(Simulator.Simulator_Off);
			} catch (StatusIdNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * TODO:
	 * 
	 * @param simulator_id
	 */
	public void doSimulations() {
		System.out.println("Number of simulation threads running: "
				+ Math.max(Thread.activeCount() - 2, 0));
		int limit = Math.max(0,
				this.Simulation_High_Limit - (Thread.activeCount() - 2));

		if (limit > 0) {
			System.out.println("Can receive a workload of " + limit
					+ " requests from database");
			System.out.println("Downloads a maximum of " + limit
					+ " requests from database...");
			ArrayList<QueueObjects> requests = getUntakenQueueObject(limit);

			if (requests.size() > 0) {
				System.out
						.println("Number of simulation requests recieved from database: "
								+ requests.size());
				System.out.println("Starting " + requests.size()
						+ " new simulation threads..");
				ExecutorService service = Executors.newFixedThreadPool(requests
						.size());

				for (int i = 0; i < requests.size(); i++) {
					Simulation simulation = new Simulation(requests.get(i));
					service.execute(simulation);
				}
				service.shutdown();
			}
		} else {
			System.out
					.println("Workload too high.. Cancelling downloading of new requests");
		}
	}

	public void sleep() {
		// sleep 10000 milliseconds
		// System.out.println("Waiting 1000 milliseconds...");
		long sleepTime = 3000l;
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<QueueObjects> getUntakenQueueObject(int limit) {
		ArrayList<QueueObjects> requests = new ArrayList<QueueObjects>();

		try {
			// System.out.println("Downloading...");
			ArrayList<Integer> requestIDs = new ArrayList<Integer>();

			int statusId = Status.getInstance().getStatusID(
					QueueObjects.Request_Pending);

			Connection connection = Settings.getDBC();
			String query = "SELECT ID FROM "
					+ "Simulator_Queue_Objects WHERE Status_ID=? limit ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, statusId);
			statement.setInt(2, limit);
			ResultSet set = statement.executeQuery();

			while (set.next()) {
				requestIDs.add(set.getInt(1));
			}

			statement.close();
			System.out.println("Received " + requestIDs.size() + " requests");

			for (int i = 0; i < requestIDs.size(); i++) {
				try {
					QueueObjects request = new QueueObjects(requestIDs.get(i));
					requests.add(request);
				} catch (ObjectNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// System.out.println("Download successful");

		} catch (SQLException ex) {
			System.out.println("Download failed");
			ex.printStackTrace();
		} catch (StatusIdNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("The ID to the status \""
					+ QueueObjects.Request_Pending + "\" was not found");
			e.printStackTrace();
		}
		return requests;
	}

	private void validateSimulatorStatus() {
		// System.out.println("Validating simulator-status...");

		ThreadGroup threadRoot = Thread.currentThread().getThreadGroup();

		int activeThreadCount = threadRoot.activeCount();
		try {
			if (activeThreadCount > this.Simulation_High_Limit) {
				System.out
						.print("The workload is too high, update simulator-status to database..");
				AliveMessenger.getInstance().setStatus(
						Simulator.Simulator_Work_Status_High);

			} else if (activeThreadCount <= this.Simulation_High_Limit
					&& activeThreadCount > this.Simulation_Low_Limit) {
				System.out
						.println("The workload is average, update simulator-status to database..");
				AliveMessenger.getInstance().setStatus(
						Simulator.Simulator_Work_Status_Medium);
			} else {
				System.out
						.println("The workload is low, update simulator-status to database..");
				AliveMessenger.getInstance().setStatus(
						Simulator_Work_Status_Low);
			}
		} catch (StatusIdNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Settings();
		AliveMessenger.getInstance();
		new Simulator();
	}

}
