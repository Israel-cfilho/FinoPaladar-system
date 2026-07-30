/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        background: "#faf7f2",
        surface: "#ffffff",
        foreground: "#1f1a17",
        muted: "#8a817a",
        border: "#e7e0d6",
        primary: {
          DEFAULT: "#c2410c",
          hover: "#9a3412",
          foreground: "#ffffff",
        },
        accent: {
          DEFAULT: "#15803d",
          foreground: "#ffffff",
        },
        danger: {
          DEFAULT: "#b91c1c",
          foreground: "#ffffff",
        },
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        serif: ["Fraunces", "Georgia", "serif"],
      },
      borderRadius: {
        DEFAULT: "0.625rem",
      },
    },
  },
  plugins: [],
}
