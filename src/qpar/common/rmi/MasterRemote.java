/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package qpar.common.rmi;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterRemote extends Remote {
	public void registerSlave(SlaveRemote ref) throws RemoteException, UnknownHostException;
	public void ping() throws RemoteException;
	public void displaySlaveMessage(String slave, String message) throws RemoteException;
	public Boolean getCachedResult(byte[] hash) throws RemoteException;
	public void cacheResult(byte[] hash, boolean result) throws RemoteException;
	public TQbfRemote getWork() throws RemoteException, InterruptedException;
}
