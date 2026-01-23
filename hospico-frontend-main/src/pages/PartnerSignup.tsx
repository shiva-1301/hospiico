import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAppDispatch } from "../store/store";
import { setCredentials } from "../features/auth/authSlice";
import { API_BASE_URL } from "../api";
import { Building2, ArrowRight, ArrowLeft, Check } from "lucide-react";

interface FormData {
    // User details
    name: string;
    email: string;
    password: string;
    phone: string;
    // Hospital details
    hospitalName: string;
    address: string;
    city: string;
    state: string;
    pincode: string;
    hospitalPhone: string;
    hospitalEmail: string;
    specializations: string[];
    description: string;
    latitude: number;
    longitude: number;
    imageUrl: string;
}

const PartnerSignup = () => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const [step, setStep] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const [formData, setFormData] = useState<FormData>({
        name: "",
        email: "",
        password: "",
        phone: "",
        hospitalName: "",
        address: "",
        city: "",
        state: "",
        pincode: "",
        hospitalPhone: "",
        hospitalEmail: "",
        specializations: [],
        description: "",
        latitude: 0,
        longitude: 0,
        imageUrl: "",
    });

    const availableSpecializations = [
        "Cardiology",
        "Orthopedics",
        "Pediatrics",
        "Dermatology",
        "Neurology",
        "Gynecology",
        "ENT",
        "General Medicine",
        "Surgery",
        "Ophthalmology",
        "Pulmonology",
        "Oncology",
    ];

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const toggleSpecialization = (spec: string) => {
        setFormData({
            ...formData,
            specializations: formData.specializations.includes(spec)
                ? formData.specializations.filter((s) => s !== spec)
                : [...formData.specializations, spec],
        });
    };

    const handleSubmit = async () => {
        setLoading(true);
        setError("");

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/partner/signup`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    ...formData,
                    specializations: formData.specializations.map((s) => ({ specialization: s })),
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Signup failed");
            }

            // Store credentials in Redux
            dispatch(
                setCredentials({
                    user: {
                        id: data.id,
                        email: data.email,
                        name: data.name,
                        role: data.role,
                    },
                    token: data.token,
                })
            );

            // Redirect to hospital dashboard
            navigate("/hospital-dashboard");
        } catch (err: any) {
            setError(err.message || "Failed to create account");
        } finally {
            setLoading(false);
        }
    };

    const nextStep = () => {
        if (step < 3) setStep(step + 1);
    };

    const prevStep = () => {
        if (step > 1) setStep(step - 1);
    };

    const isStepValid = () => {
        if (step === 1) {
            return formData.name && formData.email && formData.password && formData.phone;
        }
        if (step === 2) {
            return formData.hospitalName && formData.address && formData.city && formData.state;
        }
        return formData.specializations.length > 0;
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-slate-900 dark:to-slate-800 py-12 px-4 transition-colors duration-200">
            <div className="max-w-2xl mx-auto">
                {/* Header */}
                <div className="text-center mb-8">
                    <div className="flex justify-center mb-4">
                        <Building2 className="h-16 w-16 text-blue-600 dark:text-blue-400" />
                    </div>
                    <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
                        Partner with Us
                    </h1>
                    <p className="text-gray-600 dark:text-gray-400">
                        Join our network and grow your hospital's digital presence
                    </p>
                </div>

                {/* Progress Steps */}
                <div className="flex justify-between mb-8">
                    {[1, 2, 3].map((s) => (
                        <div key={s} className="flex items-center flex-1">
                            <div
                                className={`w-10 h-10 rounded-full flex items-center justify-center font-semibold transition-colors ${s < step
                                    ? "bg-green-500 text-white"
                                    : s === step
                                        ? "bg-blue-600 text-white"
                                        : "bg-gray-300 dark:bg-gray-600 text-gray-600 dark:text-gray-400"
                                    }`}
                            >
                                {s < step ? <Check size={20} /> : s}
                            </div>
                            {s < 3 && (
                                <div
                                    className={`flex-1 h-1 mx-2 transition-colors ${s < step ? "bg-green-500" : "bg-gray-300 dark:bg-gray-600"
                                        }`}
                                />
                            )}
                        </div>
                    ))}
                </div>

                {/* Form Card */}
                <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-xl p-8 transition-colors duration-200">
                    {error && (
                        <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-lg text-red-800 dark:text-red-300">
                            {error}
                        </div>
                    )}

                    {/* Step 1: User Details */}
                    {step === 1 && (
                        <div className="space-y-4">
                            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
                                Your Account Details
                            </h2>
                            <input
                                type="text"
                                name="name"
                                placeholder="Full Name *"
                                value={formData.name}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                            <input
                                type="email"
                                name="email"
                                placeholder="Email Address *"
                                value={formData.email}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                            <input
                                type="password"
                                name="password"
                                placeholder="Password *"
                                value={formData.password}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                            <input
                                type="tel"
                                name="phone"
                                placeholder="Phone Number *"
                                value={formData.phone}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                        </div>
                    )}

                    {/* Step 2: Hospital Details */}
                    {step === 2 && (
                        <div className="space-y-4">
                            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
                                Hospital Information
                            </h2>
                            <input
                                type="text"
                                name="hospitalName"
                                placeholder="Hospital Name *"
                                value={formData.hospitalName}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                            <input
                                type="text"
                                name="address"
                                placeholder="Address *"
                                value={formData.address}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                            <div className="grid grid-cols-2 gap-4">
                                <input
                                    type="text"
                                    name="city"
                                    placeholder="City *"
                                    value={formData.city}
                                    onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                                />
                                <input
                                    type="text"
                                    name="state"
                                    placeholder="State *"
                                    value={formData.state}
                                    onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                                />
                            </div>
                            <input
                                type="text"
                                name="pincode"
                                placeholder="Pincode"
                                value={formData.pincode}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                            <input
                                type="tel"
                                name="hospitalPhone"
                                placeholder="Hospital Phone"
                                value={formData.hospitalPhone}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                            <input
                                type="email"
                                name="hospitalEmail"
                                placeholder="Hospital Email"
                                value={formData.hospitalEmail}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                        </div>
                    )}

                    {/* Step 3: Specializations & Details */}
                    {step === 3 && (
                        <div className="space-y-4">
                            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
                                Specializations & Details
                            </h2>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                                    Select Specializations *
                                </label>
                                <div className="grid grid-cols-2 gap-2">
                                    {availableSpecializations.map((spec) => (
                                        <button
                                            key={spec}
                                            type="button"
                                            onClick={() => toggleSpecialization(spec)}
                                            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${formData.specializations.includes(spec)
                                                ? "bg-blue-600 text-white"
                                                : "bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600"
                                                }`}
                                        >
                                            {spec}
                                        </button>
                                    ))}
                                </div>
                            </div>
                            <textarea
                                name="description"
                                placeholder="Hospital Description"
                                value={formData.description}
                                onChange={handleChange}
                                rows={4}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                            <input
                                type="url"
                                name="imageUrl"
                                placeholder="Hospital Image URL (optional)"
                                value={formData.imageUrl}
                                onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                            />
                        </div>
                    )}

                    {/* Navigation Buttons */}
                    <div className="flex justify-between mt-8">
                        <button
                            onClick={prevStep}
                            disabled={step === 1}
                            className="px-6 py-3 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 transition-colors"
                        >
                            <ArrowLeft size={20} />
                            Previous
                        </button>

                        {step < 3 ? (
                            <button
                                onClick={nextStep}
                                disabled={!isStepValid()}
                                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 transition-colors"
                            >
                                Next
                                <ArrowRight size={20} />
                            </button>
                        ) : (
                            <button
                                onClick={handleSubmit}
                                disabled={!isStepValid() || loading}
                                className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 transition-colors"
                            >
                                {loading ? "Creating Account..." : "Complete Signup"}
                                <Check size={20} />
                            </button>
                        )}
                    </div>
                </div>

                {/* Footer */}
                <p className="text-center text-sm text-gray-600 dark:text-gray-400 mt-6">
                    Already have an account?{" "}
                    <a href="/partner-login" className="text-blue-600 dark:text-blue-400 hover:underline font-medium">
                        Login here
                    </a>
                </p>
            </div>
        </div>
    );
};

export default PartnerSignup;
