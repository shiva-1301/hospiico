import { useState, useEffect } from "react";
import { useSelector } from "react-redux";
import type { RootState } from "../store/store";
import { useNavigate } from "react-router-dom";
import {
    Building2,
    Calendar,
    Users,
    Star,
    Settings,
    BarChart3,
    // Clock, // Removed unused import
    MapPin,
    Phone,
    Mail
} from "lucide-react";
import { API_BASE_URL } from "../api";

interface Hospital {
    id: string;
    name: string;
    address: string;
    city: string;
    state: string;
    pincode: string;
    phone: string;
    email: string;
    description: string;
    specializations: Array<{ specialization: string }>;
    rating: number;
    imageUrl: string;
}

const HospitalDashboard = () => {
    const navigate = useNavigate();
    const { user, isAuthenticated } = useSelector((state: RootState) => state.auth);
    const [activeTab, setActiveTab] = useState("overview");
    const [hospital, setHospital] = useState<Hospital | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Check if user is authenticated and has HOSPITAL role
        if (!isAuthenticated) {
            navigate("/partner-login");
            return;
        }

        // Fetch hospital data
        fetchHospitalData();
    }, [isAuthenticated, navigate]);

    const fetchHospitalData = async () => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/api/clinics`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            const data = await response.json();

            // For now, get the first hospital (in production, filter by ownerId)
            // TODO: Add endpoint to get hospital by owner
            if (data && data.length > 0) {
                // Find hospital owned by current user
                // For now, use the last one (City General Hospital)
                setHospital(data[data.length - 1]);
            }
        } catch (error) {
            console.error("Failed to fetch hospital data:", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-slate-900">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600 dark:text-gray-400">Loading your dashboard...</p>
                </div>
            </div>
        );
    }

    if (!hospital) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-slate-900">
                <div className="text-center">
                    <Building2 className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
                        No Hospital Found
                    </h2>
                    <p className="text-gray-600 dark:text-gray-400">
                        Your hospital profile is being set up. Please contact support if this persists.
                    </p>
                </div>
            </div>
        );
    }

    const tabs = [
        { id: "overview", label: "Overview", icon: BarChart3 },
        { id: "profile", label: "Hospital Profile", icon: Building2 },
        { id: "appointments", label: "Appointments", icon: Calendar },
        { id: "doctors", label: "Doctors", icon: Users },
        { id: "reviews", label: "Reviews", icon: Star },
    ];

    return (
        <div className="min-h-screen bg-gray-50 dark:bg-slate-900 transition-colors duration-200">
            {/* Header */}
            <div className="bg-white dark:bg-slate-800 border-b border-gray-200 dark:border-gray-700 transition-colors duration-200">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
                                {hospital.name}
                            </h1>
                            <p className="text-gray-600 dark:text-gray-400 mt-1">
                                Hospital Management Dashboard
                            </p>
                        </div>
                        <div className="flex items-center gap-4">
                            <div className="text-right">
                                <p className="text-sm text-gray-600 dark:text-gray-400">Welcome back,</p>
                                <p className="font-semibold text-gray-900 dark:text-white">{user?.name || user?.email}</p>
                            </div>
                            <div className="h-12 w-12 rounded-full bg-blue-600 flex items-center justify-center text-white font-semibold text-lg">
                                {user?.name?.charAt(0) || user?.email?.charAt(0) || "H"}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Tabs */}
            <div className="bg-white dark:bg-slate-800 border-b border-gray-200 dark:border-gray-700 transition-colors duration-200">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex space-x-8 overflow-x-auto">
                        {tabs.map((tab) => {
                            const Icon = tab.icon;
                            return (
                                <button
                                    key={tab.id}
                                    onClick={() => setActiveTab(tab.id)}
                                    className={`flex items-center gap-2 px-4 py-4 border-b-2 font-medium text-sm whitespace-nowrap transition-colors ${activeTab === tab.id
                                        ? "border-blue-600 text-blue-600 dark:text-blue-400"
                                        : "border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200 hover:border-gray-300 dark:hover:border-gray-600"
                                        }`}
                                >
                                    <Icon size={20} />
                                    {tab.label}
                                </button>
                            );
                        })}
                    </div>
                </div>
            </div>

            {/* Content */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {activeTab === "overview" && <OverviewTab hospital={hospital} />}
                {activeTab === "profile" && <ProfileTab hospital={hospital} onUpdate={fetchHospitalData} />}
                {activeTab === "appointments" && <AppointmentsTab />}
                {activeTab === "doctors" && <DoctorsTab />}
                {activeTab === "reviews" && <ReviewsTab />}
            </div>
        </div>
    );
};

// Overview Tab Component
const OverviewTab = ({ hospital }: { hospital: Hospital }) => {
    const [stats, setStats] = useState({
        appointments: 0,
        doctors: 0,
        reviews: 0,
        rating: hospital.rating
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchStats();
    }, [hospital.id]);

    const fetchStats = async () => {
        try {
            const token = localStorage.getItem("jwt_token");

            // Fetch appointments for this hospital using the new role-based endpoint
            const appointmentsResponse = await fetch(
                `${API_BASE_URL}/api/dashboard/appointments/my-hospital`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            if (appointmentsResponse.ok) {
                const appointments = await appointmentsResponse.json();
                console.log("Fetched appointments:", appointments); // Debug log
                setStats(prev => ({
                    ...prev,
                    appointments: Array.isArray(appointments) ? appointments.length : 0
                }));
            } else {
                console.error("Failed to fetch appointments:", appointmentsResponse.status);
            }

            // Fetch doctors for this hospital
            const doctorsResponse = await fetch(
                `${API_BASE_URL}/api/clinics/${hospital.id}/doctors`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            if (doctorsResponse.ok) {
                const doctors = await doctorsResponse.json();
                console.log("Fetched doctors:", doctors); // Debug log
                setStats(prev => ({
                    ...prev,
                    doctors: Array.isArray(doctors) ? doctors.length : 0
                }));
            } else {
                console.error("Failed to fetch doctors:", doctorsResponse.status);
            }
        } catch (error) {
            console.error("Failed to fetch stats:", error);
        } finally {
            setLoading(false);
        }
    };

    const statsCards = [
        { label: "Total Appointments", value: stats.appointments.toString(), icon: Calendar, color: "blue" },
        { label: "Doctors", value: stats.doctors.toString(), icon: Users, color: "green" },
        { label: "Reviews", value: stats.reviews.toString(), icon: Star, color: "yellow" },
        { label: "Rating", value: stats.rating.toFixed(1), icon: Star, color: "purple" },
    ];

    return (
        <div className="space-y-6">
            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {statsCards.map((stat) => {
                    const Icon = stat.icon;
                    return (
                        <div
                            key={stat.label}
                            className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 transition-colors duration-200"
                        >
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm text-gray-600 dark:text-gray-400">{stat.label}</p>
                                    <p className="text-3xl font-bold text-gray-900 dark:text-white mt-2">
                                        {loading ? (
                                            <span className="animate-pulse">...</span>
                                        ) : (
                                            stat.value
                                        )}
                                    </p>
                                </div>
                                <div className={`p-3 rounded-full bg-${stat.color}-100 dark:bg-${stat.color}-900/30`}>
                                    <Icon className={`h-6 w-6 text-${stat.color}-600 dark:text-${stat.color}-400`} />
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Hospital Info Card */}
            <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 transition-colors duration-200">
                <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
                    Hospital Information
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="flex items-start gap-3">
                        <MapPin className="h-5 w-5 text-gray-400 mt-1" />
                        <div>
                            <p className="text-sm text-gray-600 dark:text-gray-400">Address</p>
                            <p className="text-gray-900 dark:text-white font-medium">
                                {hospital.address}, {hospital.city}, {hospital.state} - {hospital.pincode}
                            </p>
                        </div>
                    </div>
                    <div className="flex items-start gap-3">
                        <Phone className="h-5 w-5 text-gray-400 mt-1" />
                        <div>
                            <p className="text-sm text-gray-600 dark:text-gray-400">Phone</p>
                            <p className="text-gray-900 dark:text-white font-medium">{hospital.phone}</p>
                        </div>
                    </div>
                    <div className="flex items-start gap-3">
                        <Mail className="h-5 w-5 text-gray-400 mt-1" />
                        <div>
                            <p className="text-sm text-gray-600 dark:text-gray-400">Email</p>
                            <p className="text-gray-900 dark:text-white font-medium">{hospital.email}</p>
                        </div>
                    </div>
                    <div className="flex items-start gap-3">
                        <Building2 className="h-5 w-5 text-gray-400 mt-1" />
                        <div>
                            <p className="text-sm text-gray-600 dark:text-gray-400">Specializations</p>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {hospital.specializations.map((spec, idx) => (
                                    <span
                                        key={idx}
                                        className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 text-xs rounded-full"
                                    >
                                        {spec.specialization}
                                    </span>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Quick Actions */}
            <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 transition-colors duration-200">
                <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Quick Actions</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <button className="p-4 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg hover:border-blue-500 dark:hover:border-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-colors text-left">
                        <Calendar className="h-6 w-6 text-blue-600 dark:text-blue-400 mb-2" />
                        <p className="font-semibold text-gray-900 dark:text-white">View Appointments</p>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Manage patient bookings</p>
                    </button>
                    <button className="p-4 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg hover:border-green-500 dark:hover:border-green-400 hover:bg-green-50 dark:hover:bg-green-900/20 transition-colors text-left">
                        <Users className="h-6 w-6 text-green-600 dark:text-green-400 mb-2" />
                        <p className="font-semibold text-gray-900 dark:text-white">Add Doctor</p>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Expand your medical team</p>
                    </button>
                    <button className="p-4 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg hover:border-purple-500 dark:hover:border-purple-400 hover:bg-purple-50 dark:hover:bg-purple-900/20 transition-colors text-left">
                        <Settings className="h-6 w-6 text-purple-600 dark:text-purple-400 mb-2" />
                        <p className="font-semibold text-gray-900 dark:text-white">Edit Profile</p>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Update hospital details</p>
                    </button>
                </div>
            </div>
        </div>
    );
};

// Placeholder components for other tabs
const ProfileTab = ({ hospital, onUpdate }: { hospital: Hospital; onUpdate: () => void }) => (
    <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Edit Hospital Profile</h2>
        <p className="text-gray-600 dark:text-gray-400">Profile editing functionality coming soon...</p>
    </div>
);

const AppointmentsTab = () => (
    <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Appointments</h2>
        <p className="text-gray-600 dark:text-gray-400">No appointments yet. Patients can book appointments through your hospital profile.</p>
    </div>
);

const DoctorsTab = () => (
    <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Manage Doctors</h2>
        <p className="text-gray-600 dark:text-gray-400">Add doctors to your hospital to help patients find the right specialist.</p>
    </div>
);

const ReviewsTab = () => (
    <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Patient Reviews</h2>
        <p className="text-gray-600 dark:text-gray-400">No reviews yet. Reviews will appear here once patients start rating your hospital.</p>
    </div>
);

export default HospitalDashboard;
