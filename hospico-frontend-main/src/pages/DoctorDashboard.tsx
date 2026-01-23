import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import type { RootState } from '../store/store';
import { API_BASE_URL } from '../api';
import {
    Users,
    Calendar,
    Clock,
    TrendingUp,
    Activity
} from 'lucide-react';

interface DashboardStats {
    totalPatients: number;
    appointments: number;
    upcoming: number;
    completed: number;
}

interface Appointment {
    id: string;
    patientName: string;
    doctorName: string;
    specialization: string;
    appointmentDate: string;
    appointmentTime: string;
    status: string;
    symptoms: string;
}

const DoctorDashboard = () => {
    const { doctorId } = useParams();
    const navigate = useNavigate();
    const { user } = useSelector((state: RootState) => state.auth);

    const [stats, setStats] = useState<DashboardStats>({
        totalPatients: 0,
        appointments: 0,
        upcoming: 0,
        completed: 0
    });

    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);

    // Verify access
    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }

        // Allow access if user is the doctor themselves, or an admin/hospital owner
        const canAccess =
            (user.role === 'DOCTOR' && user.doctorId === doctorId) ||
            user.role === 'ADMIN' ||
            user.role === 'HOSPITAL';

        if (!canAccess) {
            navigate('/');
        }
    }, [user, doctorId, navigate]);

    useEffect(() => {
        fetchDashboardData();
    }, [doctorId]);

    const fetchDashboardData = async () => {
        try {
            const token = localStorage.getItem("jwt_token");

            // 1. Fetch Stats
            const statsResponse = await fetch(`${API_BASE_URL}/api/dashboard/stats`, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (statsResponse.ok) {
                const statsData = await statsResponse.json();
                // Since the backend returns a generic structure, we map it
                // For doctors: totalAppointments is in appointments count field
                setStats(prev => ({
                    ...prev,
                    appointments: statsData.totalAppointments || 0,
                    totalPatients: statsData.totalAppointments || 0 // Proxy for now
                }));
            }

            // 2. Fetch Appointments
            const apptResponse = await fetch(`${API_BASE_URL}/api/dashboard/appointments/my-patients`, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (apptResponse.ok) {
                const apptData = await apptResponse.json();
                setAppointments(Array.isArray(apptData) ? apptData : []);

                // Calculate derived stats
                if (Array.isArray(apptData)) {
                    const upcoming = apptData.filter(a => new Date(a.appointmentDate) >= new Date()).length;
                    setStats(prev => ({
                        ...prev,
                        upcoming,
                        completed: apptData.length - upcoming,
                        appointments: apptData.length
                    }));
                }
            }

        } catch (error) {
            console.error("Error fetching dashboard data:", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-screen bg-gray-50 dark:bg-slate-900">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-600"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 dark:bg-slate-900 pt-20 pb-10 px-4 sm:px-6 lg:px-8">
            <div className="max-w-7xl mx-auto space-y-8">

                {/* Header */}
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center gap-3">
                            <span className="p-2 bg-emerald-100 dark:bg-emerald-900/30 rounded-lg">
                                <Activity className="w-8 h-8 text-emerald-600 dark:text-emerald-400" />
                            </span>
                            Doctor Dashboard
                        </h1>
                        <p className="mt-2 text-gray-600 dark:text-slate-400">
                            Welcome back, Dr. {user?.name}
                        </p>
                    </div>

                    <div className="flex items-center gap-3 bg-white dark:bg-slate-800 p-2 rounded-xl shadow-sm border border-gray-100 dark:border-slate-700">
                        <div className="p-2 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                            <Calendar className="w-5 h-5 text-blue-600 dark:text-blue-400" />
                        </div>
                        <div className="text-sm">
                            <p className="text-gray-500 dark:text-slate-400">Today</p>
                            <p className="font-semibold text-gray-900 dark:text-white">
                                {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}
                            </p>
                        </div>
                    </div>
                </div>

                {/* Stats Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    <StatCard
                        icon={<Users className="w-6 h-6 text-emerald-600" />}
                        label="Total Patients"
                        value={stats.appointments} // Using appointment count as proxy for patients for now
                        color="bg-emerald-50 dark:bg-emerald-900/20"
                        borderColor="border-emerald-200 dark:border-emerald-800"
                    />
                    <StatCard
                        icon={<Calendar className="w-6 h-6 text-blue-600" />}
                        label="Appointments"
                        value={stats.appointments}
                        color="bg-blue-50 dark:bg-blue-900/20"
                        borderColor="border-blue-200 dark:border-blue-800"
                    />
                    <StatCard
                        icon={<Clock className="w-6 h-6 text-orange-600" />}
                        label="Upcoming"
                        value={stats.upcoming}
                        color="bg-orange-50 dark:bg-orange-900/20"
                        borderColor="border-orange-200 dark:border-orange-800"
                    />
                    <StatCard
                        icon={<TrendingUp className="w-6 h-6 text-purple-600" />}
                        label="Completed"
                        value={stats.completed}
                        color="bg-purple-50 dark:bg-purple-900/20"
                        borderColor="border-purple-200 dark:border-purple-800"
                    />
                </div>

                {/* Recent Appointments Table */}
                <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-gray-100 dark:border-slate-700 overflow-hidden">
                    <div className="p-6 border-b border-gray-100 dark:border-slate-700 flex justify-between items-center">
                        <h2 className="text-xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
                            <Users className="w-5 h-5 text-emerald-500" />
                            Patient Appointments
                        </h2>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50 dark:bg-slate-900/50">
                                <tr>
                                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-slate-400 uppercase tracking-wider">Patient Name</th>
                                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-slate-400 uppercase tracking-wider">Date & Time</th>
                                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-slate-400 uppercase tracking-wider">Symptoms</th>
                                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-slate-400 uppercase tracking-wider">Status</th>
                                    <th className="px-6 py-4 text-right text-xs font-semibold text-gray-500 dark:text-slate-400 uppercase tracking-wider">Action</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100 dark:divide-slate-700">
                                {appointments.length > 0 ? (
                                    appointments.map((appt) => (
                                        <tr key={appt.id} className="hover:bg-gray-50 dark:hover:bg-slate-700/30 transition-colors">
                                            <td className="px-6 py-4">
                                                <div className="flex items-center gap-3">
                                                    <div className="w-8 h-8 rounded-full bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center text-emerald-600 font-bold text-sm">
                                                        {appt.patientName.charAt(0)}
                                                    </div>
                                                    <span className="font-medium text-gray-900 dark:text-white">{appt.patientName}</span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="flex flex-col text-sm">
                                                    <span className="text-gray-900 dark:text-white font-medium">{appt.appointmentDate}</span>
                                                    <span className="text-gray-500 dark:text-slate-400">{appt.appointmentTime}</span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className="text-sm text-gray-600 dark:text-slate-300 max-w-xs truncate block">
                                                    {appt.symptoms || "Not specified"}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className={`px-3 py-1 rounded-full text-xs font-medium 
                          ${appt.status === 'CONFIRMED' ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' :
                                                        appt.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400' :
                                                            'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400'}`}>
                                                    {appt.status || "Scheduled"}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-right">
                                                <button className="text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300 text-sm font-medium">
                                                    View Details
                                                </button>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="px-6 py-8 text-center text-gray-500 dark:text-slate-400">
                                            No appointments found.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

const StatCard = ({ icon, label, value, color, borderColor }: any) => (
    <div className={`bg-white dark:bg-slate-800 p-6 rounded-2xl shadow-sm border ${borderColor} flex items-start justify-between hover:shadow-md transition-shadow`}>
        <div>
            <p className="text-sm font-medium text-gray-500 dark:text-slate-400">{label}</p>
            <h3 className="text-2xl font-bold text-gray-900 dark:text-white mt-2">{value}</h3>
        </div>
        <div className={`p-3 rounded-xl ${color}`}>
            {icon}
        </div>
    </div>
);

export default DoctorDashboard;
