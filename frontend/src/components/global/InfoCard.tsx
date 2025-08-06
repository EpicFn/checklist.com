

interface InfoCardProps {
    title: string;
    content: React.ReactNode;
    color?: string;
    contentColor?: string;
}

/**
 * InfoCard 컴포넌트
 * @param param0 InfoCardProps
 * @returns InfoCard JSX
 */
const InfoCard: React.FC<InfoCardProps> = ({
    title,
    content,
    color = "#fff",
    contentColor = "#222",
}) => (
    <div
        style={{
            background: color,
            borderRadius: 12,
            boxShadow: "0 2px 8px rgba(0, 0, 0, 0.185)",
            padding: "18px 20px",
            width: "100%",
            minWidth: 120,
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
            position: "relative",
            gap: 12,
        }}
    >
        <span
            style={{
                fontSize: 12,
                color: "#999",
                fontWeight: 500,
                letterSpacing: 0.2,
                marginRight: 8,
                minWidth: 56,
            }}
        >
            {title}
        </span>
        <span
            style={{
                fontSize: 15,
                color: contentColor,
                fontWeight: 700,
                wordBreak: "break-word",
            }}
        >
            {content}
        </span>
    </div>
);

export default InfoCard;